package moe.yo3explorer.skyscraper.business.boundary;

import moe.yo3explorer.dvb4j.DvbContext;
import moe.yo3explorer.skyscraper.business.control.*;
import moe.yo3explorer.skyscraper.business.entity.SatelliteEntity;
import moe.yo3explorer.skyscraper.business.entity.TransponderEntity;
import moe.yo3explorer.skyscraper.business.entity.pojo.Network;
import moe.yo3explorer.skyscraper.business.entity.pojo.Satellite;
import moe.yo3explorer.skyscraper.business.entity.pojo.Service;
import moe.yo3explorer.skyscraper.business.entity.pojo.Transponder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

public class SkyscraperService {

    private LibrarianService librarianService;
    private SkyscraperOrm orm;
    private ZapperService zapperService;
    private SkyscraperDataMiner dataMiner;
    private Logger logger;
    public static final long ONE_DAY = 1000 * 3600 * 24;

    public SkyscraperService() throws IOException, SQLException {
        orm = new SkyscraperOrm();
        zapperService = new ZapperService();
        dataMiner = new SkyscraperDataMiner(orm);
        logger = LogManager.getLogger(getClass());
        librarianService = new LibrarianService();
        logger.info("Construct Skyscraper Service");
    }

    public void perform() throws SQLException, IOException {
        List<SatelliteEntity> availableSatellites = orm.getAvailableSatellites();
        for (SatelliteEntity satelliteEntity : availableSatellites) {
            scrapeSatelliteEntity(satelliteEntity);
        }
    }

    public void scrapeSatelliteEntity(@NotNull SatelliteEntity satelliteEntity) throws SQLException, IOException {
        List<TransponderEntity> transpondersForSatellite = orm.getTranspondersForSatellite(satelliteEntity.id);
        for (TransponderEntity transponderEntity : transpondersForSatellite) {

            if (transponderEntity.lastscanned != null) {
                long now = System.currentTimeMillis();
                long backthen = transponderEntity.lastscanned.getTime();
                long difference = now - backthen;
                if (difference < ONE_DAY) {
                    logger.info(String.format("Skip transponder: %s %d/%s/%d,", satelliteEntity.name, (int) transponderEntity.frequency, transponderEntity.polarization.toString(), transponderEntity.symbolrate));
                    continue;
                }
            }

            boolean sucessful;
            logger.info(String.format("About to zap to: %s %d/%s/%d,",satelliteEntity.name,(int)transponderEntity.frequency,transponderEntity.polarization.toString(),transponderEntity.symbolrate));
            File file = zapperService.tryZapTo(satelliteEntity.diseqc, transponderEntity.frequency, transponderEntity.symbolrate, transponderEntity.polarization, transponderEntity.s2,transponderEntity.modulation);
            if (!file.exists())
                sucessful = false;
            else if (file.length() == 0)
                sucessful = false;
            else
                sucessful = true;
            logger.info(String.format("Zapping%ssucessful.",sucessful ? " " : " NOT "));

            orm.beginTransaction();
            orm.markTransponderAsScanned(transponderEntity);
            if (sucessful) {
                logger.info("Begin data mining from transport stream.");
                librarianService.catalogue(satelliteEntity,transponderEntity,file);
                orm.markTransponderAsValid(transponderEntity);
                SkyscraperDvbReceiver dvbReceiver = tryScrapeFile(file);
                List<Satellite> satellites = dvbReceiver.getSatellites();
                List<Transponder> transponders = dvbReceiver.getTransponders();
                List<Network> networks = dvbReceiver.getNetworks();
                if (satellites != null) {
                    for (Satellite satellite : satellites) {
                        dataMiner.mineFromSatellite(satellite);
                    }
                }
                else if (transponders.size() == 1)
                {
                    Transponder transponder = transponders.get(0);
                    dataMiner.mineFromTransponder(transponder,transponderEntity);
                }
                else if (satellites == null && transponders.size() == 0 && dvbReceiver.getNumEvents() == 0)
                {
                    logger.info("Transport stream does not contain any services. Discarded.");
                }
                else if (networks.size() > 0 && transponders.size() > 0)
                {
                    for (Network network : networks) {
                        dataMiner.mineFromNetwork(satelliteEntity,network);
                    }
                }
                else
                {
                    throw new RuntimeException("Don't know how to scrape from this transponder.");
                }
                logger.info("Finish data mining from transport stream.");
            }
            orm.flushTransaction();
        }
    }

    public SkyscraperDvbReceiver tryScrapeFile(File file)
    {
        try {
            return scrapeFile(file);
        } catch (IOException e) {
            throw new SkyscraperException(e);
        }
    }

    public SkyscraperDvbReceiver scrapeFile(@NotNull File file) throws IOException {
        long packages = file.length() / 188;
        FileInputStream fis = new FileInputStream(file);
        SkyscraperDvbReceiver result = scrapeInputStream(fis,packages);
        fis.close();
        return result;
    }

    public SkyscraperDvbReceiver scrapeInputStream(InputStream is, long numPackages) throws IOException {
        SkyscraperDvbReceiver dvbReceiver = new SkyscraperDvbReceiver();

        DvbContext dvbContext = new DvbContext();
        dvbContext.setDvbReceiver(dvbReceiver);
        byte[] buffer = new byte[188];
        for (long i = 0; i < numPackages; i++)
        {
            if (is.read(buffer,0,188) != 188)
                throw new IOException("incomplete read");
            dvbContext.pushPacket(buffer);
            if (dvbReceiver.getPacketLoss() == 10)
            {
                logger.info("Too much packet loss! Stop scraping early!");
                break;
            }
        }

        return dvbReceiver;
    }
}
