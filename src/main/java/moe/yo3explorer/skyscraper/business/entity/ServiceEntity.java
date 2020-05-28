package moe.yo3explorer.skyscraper.business.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import moe.yo3explorer.dvb4j.model.enums.RunningStatus;
import moe.yo3explorer.dvb4j.model.enums.ServiceType;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "services", schema = "skyscraper")
public class ServiceEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;
    @CreationTimestamp
    public Date dateadded;
    public int transponder;
    public int serviceid;
    public String name;
    @Enumerated(EnumType.STRING)
    public RunningStatus runningstatus;
    public boolean fta;
    @Enumerated(EnumType.STRING)
    public ServiceType servicetype;
    public Date lastseen;
}
