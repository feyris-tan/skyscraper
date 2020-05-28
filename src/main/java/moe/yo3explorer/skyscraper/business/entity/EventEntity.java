package moe.yo3explorer.skyscraper.business.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import moe.yo3explorer.dvb4j.model.enums.RunningStatus;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "events",schema = "skyscraper")
public class EventEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    @CreationTimestamp
    public Date dateadded;
    public int service;
    public Date starttime;
    public Date endtime;
    @Enumerated(EnumType.STRING)
    public RunningStatus runningstatus;
    public int eventid;
    public boolean encrypted;
    public String title;
    public String subtitle;
    public String synopsis;
}
