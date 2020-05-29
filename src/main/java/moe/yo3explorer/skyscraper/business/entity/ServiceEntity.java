package moe.yo3explorer.skyscraper.business.entity;

import moe.yo3explorer.dvb4j.model.enums.RunningStatus;
import moe.yo3explorer.dvb4j.model.enums.ServiceType;

import java.util.Date;

public class ServiceEntity {
    public int id;
    public Date dateadded;
    public int transponder;
    public int serviceid;
    public String name;
    public RunningStatus runningstatus;
    public boolean fta;
    public ServiceType servicetype;
    public Date lastseen;
}
