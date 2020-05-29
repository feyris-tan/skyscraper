package moe.yo3explorer.skyscraper.business.boundary;

import moe.yo3explorer.dvb4j.DvbContext;
import moe.yo3explorer.skyscraper.business.control.*;
import moe.yo3explorer.skyscraper.business.entity.SatelliteEntity;
import moe.yo3explorer.skyscraper.business.entity.TransponderEntity;
import moe.yo3explorer.skyscraper.business.entity.pojo.Satellite;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class SkyscraperService {

    private SkyscraperOrm orm;
    private ZapperService zapperService;
    private SkyscraperDataMiner dataMiner;

    public SkyscraperService() throws IOException, SQLException {
        orm = new SkyscraperOrm();
        zapperService = new ZapperService();
        dataMiner = new SkyscraperDataMiner(orm);
    }

    public void perform() throws SQLException {
        if (!zapperService.isAvailable())
            throw new NoDvbFrontendException();
        List<SatelliteEntity> availableSatellites = orm.getAvailableSatellites();
        for (SatelliteEntity satelliteEntity : availableSatellites) {
            scrapeSatelliteEntity(satelliteEntity);
        }
    }

    public void scrapeSatelliteEntity(@NotNull SatelliteEntity satelliteEntity) throws SQLException {
        List<TransponderEntity> transpondersForSatellite = orm.getTranspondersForSatellite(satelliteEntity.id);
        for (TransponderEntity transponderEntity : transpondersForSatellite) {
            boolean sucessful;
            File file = zapperService.tryZapTo(satelliteEntity.diseqc, transponderEntity.frequency, transponderEntity.symbolrate, transponderEntity.polarization, transponderEntity.s2);
            if (!file.exists())
                sucessful = false;
            else if (file.length() == 0)
                sucessful = false;
            else
                sucessful = true;

            orm.beginTransaction();
            orm.markTransponderAsScanned(transponderEntity);
            if (sucessful) {
                orm.markTransponderAsValid(transponderEntity);
                List<Satellite> satellites = tryScrapeFile(file);
                for (Satellite satellite : satellites) {
                    dataMiner.mineFromSatellite(satellite);
                }
            }
            orm.flushTransaction();
        }
    }

    public List<Satellite> tryScrapeFile(File file)
    {
        try {
            return scrapeFile(file);
        } catch (IOException e) {
            throw new SkyscraperException(e);
        }
    }

    public List<Satellite> scrapeFile(@NotNull File file) throws IOException {
        long packages = file.length() / 188;
        FileInputStream fis = new FileInputStream(file);
        return scrapeInputStream(fis,packages);
    }

    public List<Satellite> scrapeInputStream(InputStream is, long numPackages) throws IOException {
        SkyscraperDvbReceiver dvbReceiver = new SkyscraperDvbReceiver();

        DvbContext dvbContext = new DvbContext();
        dvbContext.setDvbReceiver(dvbReceiver);
        byte[] buffer = new byte[188];
        for (long i = 0; i < numPackages; i++)
        {
            if (is.read(buffer,0,188) != 188)
                throw new IOException("incomplete read");
            dvbContext.pushPacket(buffer);
        }

        return dvbReceiver.getSatellites();
    }
}
