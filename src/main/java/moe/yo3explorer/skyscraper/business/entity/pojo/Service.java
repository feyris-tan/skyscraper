package moe.yo3explorer.skyscraper.business.entity.pojo;

import moe.yo3explorer.dvb4j.model.enums.RunningStatus;
import moe.yo3explorer.dvb4j.model.enums.ServiceType;

import java.util.ArrayList;
import java.util.List;

public class Service {
    public int serviceId;
    public String channelName;
    public RunningStatus runningStatus;
    public boolean fta;
    public ServiceType serviceType;
    private List<ScheduledEvent> scheduledEventList;

    @Override
    public String toString() {
        return "Service{" +
                "serviceId=" + serviceId +
                ", channelName='" + channelName + '\'' +
                ", runningStatus=" + runningStatus +
                ", fta=" + fta +
                '}';
    }

    public void scheduleEvent(ScheduledEvent scheduledEvent) {
        if (scheduledEventList == null)
            scheduledEventList = new ArrayList<>();

        scheduledEventList.add(scheduledEvent);
    }
}
