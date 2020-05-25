package moe.yo3explorer.skyscraper.business.entity.pojo;

import moe.yo3explorer.dvb4j.model.enums.RunningStatus;

import java.util.Date;

public class ScheduledEvent
{
    public Date startTime;
    public Date endTime;
    public RunningStatus status;
    public int eventid;
    public boolean encrypted;
    public String title;
    public String subtitle;
    public String synopsis;
}
