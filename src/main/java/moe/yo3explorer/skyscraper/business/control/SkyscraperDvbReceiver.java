package moe.yo3explorer.skyscraper.business.control;

import moe.yo3explorer.dvb4j.DvbReceiver;
import moe.yo3explorer.dvb4j.PsiSection;
import moe.yo3explorer.dvb4j.decoders.DescriptorDecoder;
import moe.yo3explorer.dvb4j.model.*;
import moe.yo3explorer.dvb4j.model.descriptorEntities.ServiceListEntry;
import moe.yo3explorer.dvb4j.model.descriptors.*;
import moe.yo3explorer.skyscraper.business.entity.SatelliteEntity;
import moe.yo3explorer.skyscraper.business.entity.TransponderEntity;
import moe.yo3explorer.skyscraper.business.entity.pojo.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SkyscraperDvbReceiver implements DvbReceiver {

    private ArrayList<Network> networks;
    private ArrayList<Transponder> transponders;
    private ArrayList<Satellite> satellites;
    private long numEvents;
    private static Logger logger;
    private int packetLoss;
    private int invalidTablesEncountered;

    private @NotNull Network getNetwork(int networkId)
    {
        if (networks == null)
            networks = new ArrayList<>();

        Optional<Network> first = networks.stream().filter(x -> x.networkId == networkId).findFirst();
        if (first.isPresent())
            return first.get();
        else
        {
            Network network = new Network();
            network.networkId = networkId;
            networks.add(network);
            return network;
        }
    }

    private @NotNull Transponder getTransponder(Network network, int tsId)
    {
        if (transponders == null)
            transponders = new ArrayList<>();

        Optional<Transponder> first = transponders.stream()
                .filter(x -> x.network == network)
                .filter(x -> x.transportStreamId == tsId)
                .findFirst();

        if (first.isPresent())
            return first.get();
        else
        {
            Transponder transponder = new Transponder();
            transponder.network = network;
            transponder.transportStreamId = tsId;
            transponders.add(transponder);
            network.addTransponder(transponder);
            return transponder;
        }

    }

    private @NotNull Satellite getSatellite(double orbital, char cardinal)
    {
        if (satellites == null)
            satellites = new ArrayList<>();

        Optional<Satellite> first = satellites.stream()
                .filter(x -> x.orbitalPosition == orbital)
                .filter(x -> x.cardinalDirection == cardinal)
                .findFirst();

        if (first.isPresent())
            return first.get();
        else
        {
            Satellite satellite = new Satellite();
            satellite.cardinalDirection = cardinal;
            satellite.orbitalPosition = orbital;
            satellites.add(satellite);
            return satellite;
        }
    }

    public SkyscraperDvbReceiver() {
        if (logger == null)
        {
            logger = LogManager.getLogger(getClass());
            logger.info("DVB receiver initialized.");
        }
    }

    @Override public void onNewPatEntry(@NotNull PATEntry patEntry) {
    }
    @Override public void onNewPmtEntry(int i, PMTEntry pmtEntry) {}
    @Override public void onTdtTime(Date date) { }

    @Override
    public void onSdtEntry(@NotNull SDTEntry sdtEntry) {
        Network network = getNetwork(sdtEntry.getOriginalNetworkId());
        Transponder transponder = getTransponder(network,sdtEntry.getTsId());
        Service service = transponder.getService(sdtEntry.getServiceId());
        service.channelName = sdtEntry.getChannelName();
        service.runningStatus = sdtEntry.getRunningStatus();
        service.fta = !sdtEntry.isFreeCaMode();
    }

    @Override
    public void onNewCaDescriptor(CaDescriptor caDescriptor) { }

    @Override
    public void onTotTime(Date date, List<Descriptor> list) { }

    @Override
    public void onBouquetAssociation(@NotNull BATEntry batEntry) {
        Network network = getNetwork(batEntry.getOriginalNetworkId());
        Service service = network.getService(batEntry.getServiceId());
        service.serviceType = batEntry.getServiceType();
        Transponder transponder = getTransponder(network,batEntry.getTransportStreamId());
        transponder.addService(service);

        ServiceListDescriptor descriptorFromList = DescriptorDecoder.getDescriptorFromList(batEntry.getTsDescriptors(), ServiceListDescriptor.class);
        for (ServiceListEntry descriptorFromListService : descriptorFromList.getServices()) {
            Service service1 = network.getService(descriptorFromListService.getServiceId());
            service1.serviceType = descriptorFromListService.getServiceType();
            transponder.addService(service1);
        }

    }

    @Override
    public void onNetworkInformation(@NotNull SatelliteDeliverySystemDescriptor satelliteDeliverySystemDescriptor, List<Descriptor> tsDescriptors, List<Descriptor> networkDescriptors,NITMetadata nitMetadata) {
        double orbitalPosition = satelliteDeliverySystemDescriptor.getOrbitalPosition();
        char cardinalDirection = satelliteDeliverySystemDescriptor.isEast() ? 'E' : 'W';
        Satellite satellite = getSatellite(orbitalPosition,cardinalDirection);

        NetworkNameDescriptor descriptorFromList = DescriptorDecoder.getDescriptorFromList(networkDescriptors, NetworkNameDescriptor.class);
        if (descriptorFromList != null) {
            if (descriptorFromList.getName() != null)
                satellite.name = descriptorFromList.getName();
        }

        Network network = getNetwork(nitMetadata.getOriginalNetworkId());
        Transponder transponder = getTransponder(network,nitMetadata.getTransportStreamId());
        satellite.addTransponder(transponder);
        transponder.frequency = satelliteDeliverySystemDescriptor.getFrequency();
        transponder.polarization = satelliteDeliverySystemDescriptor.getPolarization();
        transponder.symbolrate = satelliteDeliverySystemDescriptor.getSymbolRate();
        transponder.fec = satelliteDeliverySystemDescriptor.getFec();
        transponder.modulation = satelliteDeliverySystemDescriptor.getModulationType();
        transponder.s2 = satelliteDeliverySystemDescriptor.isS2();
    }

    @Override
    public void onScheduledEvent(@NotNull EITEvent eitEvent) {
        Network network = getNetwork(eitEvent.getOriginalNetworkid());
        Transponder transponder = getTransponder(network,eitEvent.getTransportStreamId());
        Service service = transponder.getService(eitEvent.getServiceId());

        ScheduledEvent scheduledEvent = new ScheduledEvent();
        scheduledEvent.status = eitEvent.getRunningStatus();
        scheduledEvent.endTime = new Date(eitEvent.getStartTime().getTime() + eitEvent.getDuration());
        scheduledEvent.startTime = eitEvent.getStartTime();
        scheduledEvent.eventid = eitEvent.getEventId();
        scheduledEvent.encrypted = eitEvent.isEncrypted();
        service.scheduleEvent(scheduledEvent);

        ShortEventDescriptor shortEvent = DescriptorDecoder.getDescriptorFromList(eitEvent.getDescriptorList(), ShortEventDescriptor.class);
        if (shortEvent != null)
        {
            scheduledEvent.title = shortEvent.getEventName();
            scheduledEvent.subtitle = shortEvent.getText();
        }

        if (eitEvent.getDescriptorList() == null)
        {
            logger.warn("Found broken EIT Event!");
            return;
        }

        ExtendedEventDescriptor extendedEvent = DescriptorDecoder.getDescriptorFromList(eitEvent.getDescriptorList(), ExtendedEventDescriptor.class);
        if (extendedEvent != null)
        {
            scheduledEvent.synopsis = extendedEvent.getText();
        }

        numEvents++;
    }

    @Override
    public void onPacketLoss(int i, int i1, int i2) {
        packetLoss++;
        logger.warn(String.format("Packet loss! (%d)",packetLoss));
    }

    @Override
    public void onUnknownPsi(int i, @NotNull PsiSection psiSection) {
        int tableId = psiSection.getTableId();
        if (tableId >= 0x04 && tableId <= 0x3f)
            invalidTablesEncountered++;
        switch (tableId)
        {
            case 0x43:
            case 0x44:
            case 0x45:
            case 0x47:
            case 0x48:
            case 0x49:
            case 0x7c:
            case 0x7d:
            case 0xff:
                invalidTablesEncountered++;
            default:
                break;
        }
    }

    public ArrayList<Satellite> getSatellites() {
        return satellites;
    }

    public long getNumEvents() {
        return numEvents;
    }

    public List<Transponder> getTransponders()
    {
        if (transponders == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(transponders);
    }

    public List<Network> getNetworks()
    {
        if (networks == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(networks);
    }

    public int getPacketLoss() {
        return packetLoss;
    }

    public int getInvalidTablesEncountered() {
        return invalidTablesEncountered;
    }
}
