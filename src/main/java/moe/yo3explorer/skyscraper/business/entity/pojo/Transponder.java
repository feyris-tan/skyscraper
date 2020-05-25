package moe.yo3explorer.skyscraper.business.entity.pojo;

import moe.yo3explorer.dvb4j.model.enums.FEC;
import moe.yo3explorer.dvb4j.model.enums.ModulationType;
import moe.yo3explorer.dvb4j.model.enums.Polarization;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Transponder {
    public Network network;
    public int transportStreamId;
    public double frequency;
    public Polarization polarization;
    public int symbolrate;
    public FEC fec;
    public ModulationType modulation;
    public boolean s2;
    private List<Service> services;

    public void addService(Service service) {
        if (services == null)
            services = new ArrayList<>();
        Optional<Service> first = services.stream().filter(x -> x.serviceId == service.serviceId).findFirst();
        if (first.isEmpty())
            services.add(service);
    }

    public Service getService(int serviceId)
    {
        if (services == null)
            services = new ArrayList<>();
        Optional<Service> first = services.stream().filter(x -> x.serviceId == serviceId).findFirst();
        if (first.isPresent())
        {
            return first.get();
        } else {
            Service service = new Service();
            service.serviceId = serviceId;
            services.add(service);
            return service;
        }
    }

    public Iterable<Service> listServices()
    {
        if (services == null)
            return null;
        return services;
    }

    @Override
    public String toString() {
        return "Transponder{" +
                "frequency=" + frequency +
                ", polarization=" + polarization +
                ", symbolrate=" + symbolrate +
                '}';
    }
}
