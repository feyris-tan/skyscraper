package moe.yo3explorer.skyscraper.business.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Network {
    public Service getService(int serviceId)
    {
        if (services == null)
            services = new ArrayList<>();

        Optional<Service> first = services.stream().filter(x -> x.serviceId == serviceId).findFirst();
        if (first.isPresent())
            return first.get();
        else
        {
            Service service = new Service();
            service.serviceId = serviceId;
            services.add(service);
            return service;
        }
    }

    public void addTransponder(Transponder transponder)
    {
        if (transponder.network != this)
            return;
        if (transponders == null)
            transponders = new ArrayList<>();
        transponders.add(transponder);

        if (services == null)
            services = new ArrayList<>();
        if (transponder.listServices() != null)
            transponder.listServices().forEach(x -> services.add(x));
    }

    public int networkId;
    private List<Service> services;
    private List<Transponder> transponders;

    @Override
    public String toString() {
        return "Network{" +
                "networkId=" + networkId +
                ", services=" + services +
                '}';
    }
}
