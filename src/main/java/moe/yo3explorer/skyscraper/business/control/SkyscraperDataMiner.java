package moe.yo3explorer.skyscraper.business.control;

import moe.yo3explorer.skyscraper.business.entity.SatelliteEntity;
import moe.yo3explorer.skyscraper.business.entity.ServiceEntity;
import moe.yo3explorer.skyscraper.business.entity.TransponderEntity;
import moe.yo3explorer.skyscraper.business.entity.pojo.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class SkyscraperDataMiner {
    private SkyscraperOrm orm;
    private Logger logger;
    public SkyscraperDataMiner(SkyscraperOrm orm) {
        this.orm = orm;
        this.logger = LogManager.getLogger(getClass());
    }

    public void mineFromSatellite(@NotNull Satellite satellite) throws SQLException {
        SatelliteEntity satelliteEntity = orm.getSatelliteByOrbitalPosition(satellite.orbitalPosition, new String(new char[]{satellite.cardinalDirection}));
        if (satelliteEntity == null)
        {
            satelliteEntity = new SatelliteEntity();
            satelliteEntity.dateadded = new Date();
            satelliteEntity.orbitalposition = satellite.orbitalPosition;
            satelliteEntity.cardinaldirection = new String(new char[] {satellite.cardinalDirection});
            satelliteEntity.name = satellite.name;
            orm.persistSatellite(satelliteEntity);
        }

        List<Transponder> transponders = satellite.getTransponders();
        for (Transponder transponder : transponders)
        {
            TransponderEntity transponderEntity = orm.getTransponder(transponder,satelliteEntity);
            if (transponderEntity == null)
            {
                transponderEntity = orm.createTransponder(transponder,satelliteEntity);
            }
            mineFromTransponder(transponder, transponderEntity);
        }
    }

    public void mineFromTransponder(Transponder transponder, TransponderEntity transponderEntity) throws SQLException {
        if (transponder.listServices() == null)
            return;
        for (Service service : transponder.listServices()) {
            ServiceEntity serviceEntity = orm.getService(service,transponderEntity);
            if (serviceEntity == null)
            {
                serviceEntity = orm.createService(service,transponderEntity);
            }
            orm.markServiceAsSeen(serviceEntity);
            int totalEvents = 0;
            int newOnes = 0;
            int knownOnes = 0;

            for (ScheduledEvent scheduledEvent : service.listScheduledEvents()) {
                totalEvents++;
                if (!orm.testForScheduledEvent(scheduledEvent,serviceEntity))
                {
                    orm.createScheduledEvent(scheduledEvent,serviceEntity);
                    newOnes++;
                }
                else
                {
                    logger.trace(String.format("Scheduled event \"%s\" already known.",scheduledEvent.title));
                    knownOnes++;
                }
            }
            if (totalEvents > 0)
            {
                logger.info(String.format("Got %d events on %s. (%d new, %d known)",totalEvents,serviceEntity.name,newOnes,knownOnes));
            }
        }
    }

    public void mineFromNetwork(SatelliteEntity satelliteEntity, @NotNull Network network) throws SQLException {
        List<Transponder> transponders = network.getTransponders();
        for (Transponder transponder : transponders) {
            TransponderEntity transponderBySatelliteAndTsId = orm.findTransponderBySatelliteAndTsId(satelliteEntity, transponder.transportStreamId);
            if (transponderBySatelliteAndTsId != null)
            {
                mineFromTransponder(transponder,transponderBySatelliteAndTsId);
            }
        }
    }
}
