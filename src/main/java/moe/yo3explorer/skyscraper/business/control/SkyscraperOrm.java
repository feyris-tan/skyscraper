package moe.yo3explorer.skyscraper.business.control;

import moe.yo3explorer.dvb4j.model.enums.FEC;
import moe.yo3explorer.dvb4j.model.enums.ModulationType;
import moe.yo3explorer.dvb4j.model.enums.Polarization;
import moe.yo3explorer.skyscraper.business.entity.AuditOperation;
import moe.yo3explorer.skyscraper.business.entity.SatelliteEntity;
import moe.yo3explorer.skyscraper.business.entity.TransponderEntity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class SkyscraperOrm
{
    public SkyscraperOrm() throws IOException, SQLException {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("sql.properties");
        Properties properties = new Properties();
        properties.load(resourceAsStream);
        resourceAsStream.close();

        connection = DriverManager.getConnection(properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"));
        connection.setSchema("skyscraper");
    }

    private Connection connection;
    
    public List<SatelliteEntity> getAvailableSatellites() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM satellites WHERE diseqc IS NOT NULL");
        ResultSet resultSet = preparedStatement.executeQuery();
        LinkedList<SatelliteEntity> satelliteEntities = new LinkedList<>();
        while (resultSet.next())
        {
            SatelliteEntity satelliteEntity = new SatelliteEntity();
            satelliteEntity.id = resultSet.getInt(1);
            satelliteEntity.dateadded = resultSet.getDate(2);
            satelliteEntity.orbitalposition = resultSet.getDouble(3);
            satelliteEntity.cardinaldirection = resultSet.getString(4);
            satelliteEntity.name = resultSet.getString(5);
            satelliteEntity.diseqc = resultSet.getInt(6);
            satelliteEntities.add(satelliteEntity);
        }
        resultSet.close();
        preparedStatement.close();
        return satelliteEntities;
    }

    public List<TransponderEntity> getTranspondersForSatellite(int satId) throws SQLException {
        LinkedList<TransponderEntity> result = new LinkedList<>();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM transponders WHERE satellite = ?");
        preparedStatement.setInt(1,satId);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
        {
            TransponderEntity transponderEntity = new TransponderEntity();
            transponderEntity.id = resultSet.getInt(1);
            transponderEntity.dateadded = resultSet.getDate(2);
            transponderEntity.frequency = resultSet.getDouble(3);
            transponderEntity.polarization = Polarization.valueOf(resultSet.getString(4));
            transponderEntity.symbolrate = resultSet.getInt(5);
            transponderEntity.fec = FEC.valueOf(resultSet.getString(6));
            transponderEntity.modulation = ModulationType.valueOf(resultSet.getString(7));
            transponderEntity.s2 = resultSet.getBoolean(8);
            transponderEntity.satellite = resultSet.getInt(9);

            Object network = resultSet.getObject(10);
            if (network != null)
                transponderEntity.network = (Integer)network;

            Object transportstream = resultSet.getObject(11);
            if (transportstream != null)
                transponderEntity.transportstream = (Integer)transportstream;

            Object lastscanned = resultSet.getObject(12);
            if (lastscanned != null)
                transponderEntity.lastscanned = (Date)lastscanned;

            Object lastvalid = resultSet.getObject(13);
            if (lastvalid != null)
                transponderEntity.lastvalid = (java.sql.Date)lastvalid;

            result.add(transponderEntity);
        }
        resultSet.close();
        preparedStatement.close();
        return result;
    }

    public SatelliteEntity getSatelliteByOrbitalPosition(double orbital, String cardinal) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM satellites WHERE orbitalposition = ? AND cardinaldirection = ?");
        ps.setDouble(1,orbital);
        ps.setString(2,cardinal);
        ResultSet resultSet = ps.executeQuery();
        SatelliteEntity result;
        if (resultSet.next())
        {
            result = new SatelliteEntity();
            result.id = resultSet.getInt(1);
            result.dateadded = resultSet.getDate(2);
            result.orbitalposition = resultSet.getDouble(3);
            result.cardinaldirection = resultSet.getString(4);
            result.name = resultSet.getString(5);
            result.diseqc = resultSet.getInt(6);
        }
        else
        {
            result = null;
        }
        resultSet.close();
        ps.close();
        return result;
    }

    public void persistSatellite(@NotNull SatelliteEntity satelliteEntity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO satellites (orbitalposition, cardinaldirection, name) VALUES (?,?,?) RETURNING *");
        preparedStatement.setDouble(1,satelliteEntity.orbitalposition);
        preparedStatement.setString(2,satelliteEntity.cardinaldirection);
        preparedStatement.setString(3,satelliteEntity.name);

        ResultSet resultSet = preparedStatement.executeQuery();
        boolean next = resultSet.next();
        satelliteEntity.id = resultSet.getInt(1);
        satelliteEntity.dateadded = resultSet.getDate(2);

        audit(AuditOperation.ADD,SatelliteEntity.class,satelliteEntity.name);
    }

    public void audit(@NotNull AuditOperation operation, @NotNull Class entityType, String text) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO audit(operation, entitytype, text) VALUES (?,?,?)");
        preparedStatement.setString(1,operation.toString());
        preparedStatement.setString(2,entityType.getSimpleName());
        preparedStatement.setString(3,text);
        preparedStatement.executeUpdate();
    }

    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    public void flushTransaction() throws SQLException {
        connection.commit();
    }

    public void rollbackTransaction() throws SQLException {
        connection.rollback();
    }

    public void markTransponderAsScanned(@NotNull TransponderEntity transponderEntity) throws SQLException {
        transponderEntity.lastscanned = new java.sql.Date(new java.util.Date().getTime());

        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE transponders SET lastscanned = ? WHERE id = ?");
        preparedStatement.setDate(1,transponderEntity.lastscanned);
        preparedStatement.setInt(2,transponderEntity.id);
        preparedStatement.executeUpdate();
    }

    public void markTransponderAsValid(@NotNull TransponderEntity transponderEntity) throws SQLException {
        transponderEntity.lastvalid = new java.sql.Date(new java.util.Date().getTime());

        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE transponders SET lastvalid = ? WHERE id = ?");
        preparedStatement.setDate(1,transponderEntity.lastvalid);
        preparedStatement.setInt(2,transponderEntity.id);
        preparedStatement.executeUpdate();
    }
}
