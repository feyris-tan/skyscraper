package moe.yo3explorer.skyscraper.business.entity;

import moe.yo3explorer.dvb4j.model.enums.RunningStatus;

import java.sql.Timestamp;
import java.util.Date;

public class EventEntity {
    public long id;
    public Timestamp dateadded;
    public int service;
    public Timestamp starttime;
    public Timestamp endtime;
    public RunningStatus runningstatus;
    public int eventid;
    public boolean encrypted;
    public String title;
    public String subtitle;
    public String synopsis;
}
