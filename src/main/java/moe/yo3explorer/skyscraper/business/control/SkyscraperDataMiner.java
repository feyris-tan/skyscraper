package moe.yo3explorer.skyscraper.business.control;

import moe.yo3explorer.skyscraper.business.entity.SatelliteEntity;
import moe.yo3explorer.skyscraper.business.entity.pojo.Satellite;
import moe.yo3explorer.skyscraper.business.entity.pojo.Transponder;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class SkyscraperDataMiner {
    private SkyscraperOrm orm;
    public SkyscraperDataMiner(SkyscraperOrm orm) {
        this.orm = orm;
    }

    public void mineFromSatellite(Satellite satellite) throws SQLException {
        SatelliteEntity satelliteEntity = orm.getSatelliteByOrbitalPosition(satellite.orbitalPosition, new String(new char[]{satellite.cardinalDirection}));
        if (satelliteEntity == null)
        {
            satelliteEntity = new SatelliteEntity();
            satelliteEntity.dateadded = new Date();
            satelliteEntity.orbitalposition = satellite.orbitalPosition;
            satelliteEntity.cardinaldirection = new String(new char[] {satellite.cardinalDirection});
            satelliteEntity.name = satellite.name;
        }

        List<Transponder> transponders = satellite.getTransponders();
        for (Transponder transponder : transponders) {
        }

    }
}
