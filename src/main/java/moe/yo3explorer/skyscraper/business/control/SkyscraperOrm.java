package moe.yo3explorer.skyscraper.business.control;

import moe.yo3explorer.dvb4j.model.enums.*;
import moe.yo3explorer.skyscraper.business.entity.AuditOperation;
import moe.yo3explorer.skyscraper.business.entity.SatelliteEntity;
import moe.yo3explorer.skyscraper.business.entity.ServiceEntity;
import moe.yo3explorer.skyscraper.business.entity.TransponderEntity;
import moe.yo3explorer.skyscraper.business.entity.pojo.ScheduledEvent;
import moe.yo3explorer.skyscraper.business.entity.pojo.Service;
import moe.yo3explorer.skyscraper.business.entity.pojo.Transponder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class SkyscraperOrm
{
    public SkyscraperOrm() throws IOException, SQLException {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("sql.properties");
        Properties properties = new Properties();
        properties.load(resourceAsStream);
        resourceAsStream.close();

        Properties postgresProp = new Properties();
        postgresProp.put("user",properties.getProperty("username"));
        postgresProp.put("password",properties.getProperty("password"));
        postgresProp.put("ApplicationName","Skyscraper");

        connection = DriverManager.getConnection(properties.getProperty("url"), postgresProp);

        connection.setSchema("skyscraper");

        logger = LogManager.getLogger(getClass());
        logger.info("Create ORM!");
    }

    private Logger logger;
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
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM transponders WHERE satellite = ? AND ignore = FALSE ORDER BY id");
        preparedStatement.setInt(1,satId);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
        {
            TransponderEntity transponderEntity = new TransponderEntity();
            transponderEntity.id = resultSet.getInt(1);
            transponderEntity.dateadded = resultSet.getTimestamp(2);
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
                transponderEntity.lastscanned = (Timestamp)lastscanned;

            Object lastvalid = resultSet.getObject(13);
            if (lastvalid != null)
                transponderEntity.lastvalid = (Timestamp) lastvalid;

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
        if (satelliteEntity.name == null)
            satelliteEntity.name = "";
        if (satelliteEntity.name.contains("\0"))
            satelliteEntity.name = satelliteEntity.name.replace("\0","");

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

        logger.info(String.format("%s %s %s",operation.toString(),entityType.getSimpleName(),text));
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
        transponderEntity.lastscanned = new Timestamp(System.currentTimeMillis());

        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE transponders SET lastscanned = ? WHERE id = ?");
        preparedStatement.setTimestamp(1,transponderEntity.lastscanned);
        preparedStatement.setInt(2,transponderEntity.id);
        preparedStatement.executeUpdate();
    }

    public void markTransponderAsValid(@NotNull TransponderEntity transponderEntity) throws SQLException {
        transponderEntity.lastvalid = new Timestamp(System.currentTimeMillis());

        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE transponders SET lastvalid = ? WHERE id = ?");
        preparedStatement.setTimestamp(1,transponderEntity.lastvalid);
        preparedStatement.setInt(2,transponderEntity.id);
        preparedStatement.executeUpdate();
    }

    public TransponderEntity getTransponder(@NotNull Transponder transponder, @NotNull SatelliteEntity satelliteEntity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM transponders WHERE satellite = ? AND frequency = ? AND polarization = ?");
        preparedStatement.setInt(1,satelliteEntity.id);
        preparedStatement.setDouble(2,transponder.frequency);
        preparedStatement.setString(3,transponder.polarization.toString());
        TransponderEntity result;
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next())
        {
            result = new TransponderEntity();
            result.id = resultSet.getInt(1);
            result.dateadded = resultSet.getTimestamp(2);
            result.frequency = resultSet.getDouble(3);
            result.polarization = Polarization.valueOf(resultSet.getString(4));
            result.symbolrate = resultSet.getInt(5);
            result.fec = FEC.valueOf(resultSet.getString(6));
            result.modulation = ModulationType.valueOf(resultSet.getString(7));
            result.s2 = resultSet.getBoolean(8);
            result.satellite = resultSet.getInt(9);

            Object network = resultSet.getObject(10);
            if (network != null)
                result.network = (Integer)network;

            Object transportstream = resultSet.getObject(11);
            if (transportstream != null)
                result.transportstream = (Integer)transportstream;

            Object lastscanned = resultSet.getObject(12);
            if (lastscanned != null)
                result.lastscanned = (Timestamp) lastscanned;

            Object lastvalid = resultSet.getObject(13);
            if (lastvalid != null)
                result.lastvalid = (Timestamp) lastvalid;
        }
        else
        {
            result = null;
        }
        resultSet.close();
        preparedStatement.close();
        return result;
    }

    public TransponderEntity createTransponder(@NotNull Transponder transponder, @NotNull SatelliteEntity satelliteEntity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO transponders (frequency, polarization, symbolrate, fec, modulation, s2, satellite, network, transportstream) VALUES (?,?,?,?,?,?,?,?,?) RETURNING *");
        preparedStatement.setDouble(1,transponder.frequency);
        preparedStatement.setString(2,transponder.polarization.toString());
        preparedStatement.setInt(3,transponder.symbolrate);
        preparedStatement.setString(4,transponder.fec.toString());
        preparedStatement.setString(5,transponder.modulation.toString());
        preparedStatement.setBoolean(6,transponder.s2);
        preparedStatement.setInt(7,satelliteEntity.id);
        preparedStatement.setInt(8,transponder.network.networkId);
        preparedStatement.setInt(9,transponder.transportStreamId);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();

        TransponderEntity result = new TransponderEntity();
        result.id = resultSet.getInt(1);
        result.dateadded = resultSet.getTimestamp(2);
        result.frequency = resultSet.getDouble(3);
        result.polarization = Polarization.valueOf(resultSet.getString(4));
        result.symbolrate = resultSet.getInt(5);
        result.fec = FEC.valueOf(resultSet.getString(6));
        result.modulation = ModulationType.valueOf(resultSet.getString(7));
        result.s2 = resultSet.getBoolean(8);
        result.satellite = resultSet.getInt(9);

        Object network = resultSet.getObject(10);
        if (network != null)
            result.network = (Integer)network;

        Object transportstream = resultSet.getObject(11);
        if (transportstream != null)
            result.transportstream = (Integer)transportstream;

        Object lastscanned = resultSet.getObject(12);
        if (lastscanned != null)
            result.lastscanned = (Timestamp) lastscanned;

        Object lastvalid = resultSet.getObject(13);
        if (lastvalid != null)
            result.lastvalid = (Timestamp) lastvalid;

        audit(AuditOperation.ADD,Transponder.class,String.format("%s %.3f/%s/%d",satelliteEntity.name,result.frequency,result.polarization.toString(),result.symbolrate));
        return result;
    }

    public ServiceEntity getService(@NotNull Service service, @NotNull TransponderEntity transponder) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM services WHERE transponder = ? AND serviceid = ?");
        ps.setInt(1,transponder.id);
        ps.setInt(2,service.serviceId);
        ResultSet resultSet = ps.executeQuery();
        ServiceEntity result;
        if (resultSet.next())
        {
            result = new ServiceEntity();
            result.id = resultSet.getInt(1);
            result.dateadded = resultSet.getDate(2);
            result.transponder = resultSet.getInt(3);
            result.serviceid = resultSet.getInt(4);
            result.name = resultSet.getString(5);
            result.runningstatus = RunningStatus.valueOf(resultSet.getString(6));
            result.fta = resultSet.getBoolean(7);
            String serviceTypeString = resultSet.getString(8);
            if (serviceTypeString != null)
                if (!serviceTypeString.equals(""))
                    result.servicetype = ServiceType.valueOf(resultSet.getString(8));

            Object lastseen = resultSet.getObject(9);
            if (lastseen != null)
                result.lastseen = (Timestamp)lastseen;
        }
        else
        {
            result = null;
        }
        return result;
    }

    public ServiceEntity createService(@NotNull Service service, @NotNull TransponderEntity transponderEntity) throws SQLException {
        if (service.channelName == null)
            service.channelName = "";
        if (service.channelName.contains("\0"))
            service.channelName = service.channelName.replace("\0","");
        if (service.runningStatus == null)
            service.runningStatus = RunningStatus.UNDEFINED;

        PreparedStatement ps = connection.prepareStatement("INSERT INTO services (transponder, serviceid, name, runningstatus, fta, servicetype) VALUES (?,?,?,?,?,?) RETURNING *");
        ps.setInt(1,transponderEntity.id);
        ps.setInt(2,service.serviceId);
        ps.setString(3,service.channelName);
        ps.setString(4,service.runningStatus.toString());
        ps.setBoolean(5,service.fta);
        ps.setString(6,service.serviceType != null ? service.serviceType.toString() : "");

        ResultSet resultSet = ps.executeQuery();
        resultSet.next();

        ServiceEntity result = new ServiceEntity();
        result.id = resultSet.getInt(1);
        result.dateadded = resultSet.getDate(2);
        result.transponder = resultSet.getInt(3);
        result.serviceid = resultSet.getInt(4);
        result.name = resultSet.getString(5);
        result.runningstatus = RunningStatus.valueOf(resultSet.getString(6));
        result.fta = resultSet.getBoolean(7);
        String serviceTypeString = resultSet.getString(8);
        if (serviceTypeString != null)
            if (!serviceTypeString.equals(""))
                result.servicetype = ServiceType.valueOf(resultSet.getString(8));

        Object lastseen = resultSet.getObject(9);
        if (lastseen != null)
            result.lastseen = (Timestamp)lastseen;

        audit(AuditOperation.ADD,Service.class,service.channelName);
        return result;
    }

    public boolean testForScheduledEvent(@NotNull ScheduledEvent scheduledEvent, @NotNull ServiceEntity serviceEntity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT dateadded FROM events WHERE service = ? AND starttime = ? AND eventid = ?");
        preparedStatement.setInt(1,serviceEntity.id);
        preparedStatement.setTimestamp(2,new Timestamp(scheduledEvent.startTime.getTime()));
        preparedStatement.setInt(3,scheduledEvent.eventid);
        ResultSet resultSet = preparedStatement.executeQuery();
        boolean result = resultSet.next();
        resultSet.close();
        preparedStatement.close();
        return result;
    }

    public void createScheduledEvent(@NotNull ScheduledEvent scheduledEvent, @NotNull ServiceEntity serviceEntity) throws SQLException {
        if (scheduledEvent.title != null)
            if (scheduledEvent.title.contains("\0"))
                scheduledEvent.title = scheduledEvent.title.replace("\0","");
        if (scheduledEvent.subtitle != null)
            if (scheduledEvent.subtitle.contains("\0"))
                scheduledEvent.subtitle = scheduledEvent.subtitle.replace("\0","");
        if (scheduledEvent.synopsis != null)
            if (scheduledEvent.synopsis.contains("\0"))
                scheduledEvent.synopsis = scheduledEvent.synopsis.replace("\0","");

        PreparedStatement ps = connection.prepareStatement("INSERT INTO events (service, starttime, endtime, runningstatus, eventid, encrypted, title, subtitle, synopsis) VALUES (?,?,?,?,?,?,?,?,?)");
        ps.setInt(1,serviceEntity.id);
        ps.setTimestamp(2,new Timestamp(scheduledEvent.startTime.getTime()));
        ps.setTimestamp(3,new Timestamp(scheduledEvent.endTime.getTime()));
        ps.setString(4,scheduledEvent.status.toString());
        ps.setInt(5,scheduledEvent.eventid);
        ps.setBoolean(6,scheduledEvent.encrypted);
        ps.setString(7,scheduledEvent.title);
        ps.setString(8,scheduledEvent.subtitle);
        ps.setString(9,scheduledEvent.synopsis);
        ps.executeUpdate();
    }

    public void markServiceAsSeen(@NotNull ServiceEntity serviceEntity) throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        PreparedStatement ps = connection.prepareStatement("UPDATE services SET lastseen = ? WHERE id = ?");
        ps.setTimestamp(1,timestamp);
        ps.setInt(2,serviceEntity.id);
        ps.executeUpdate();
        serviceEntity.lastseen = timestamp;
    }

    public TransponderEntity findTransponderBySatelliteAndTsId(@NotNull SatelliteEntity satelliteEntity, int tsId) throws SQLException {
        List<TransponderEntity> transpondersForSatellite = getTranspondersForSatellite(satelliteEntity.id);
        Optional<TransponderEntity> first = transpondersForSatellite.stream().filter(x -> x.transportstream == tsId).findFirst();
        return first.orElse(null);
    }

    public void setChannelName(@NotNull ServiceEntity entity, String name) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE services SET name = ? WHERE id = ?");
        preparedStatement.setString(1,name);
        preparedStatement.setInt(2,entity.id);
        int i = preparedStatement.executeUpdate();
        if (i != 1)
            throw new RuntimeException("setChannelName failed");

        audit(AuditOperation.UPDATE,Service.class,String.format("Channel Name for service %d is now %s",entity.id,name));
        entity.name = name;
    }
}
