package moe.yo3explorer.skyscraper.business.boundary;

import moe.yo3explorer.dvb4j.DvbContext;
import moe.yo3explorer.skyscraper.business.control.AuditService;
import moe.yo3explorer.skyscraper.business.control.SkyscraperDvbReceiver;
import moe.yo3explorer.skyscraper.business.entity.pojo.Satellite;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.List;

@Singleton
public class SkyscraperService {
    @Inject
    AuditService auditService;

    public List<Satellite> scrapeFile(@NotNull File file) throws IOException {
        long packages = file.length() / 188;
        FileInputStream fis = new FileInputStream(file);
        return scrapeInputStream(fis,packages);
    }

    public List<Satellite> scrapeInputStream(InputStream is, long numPackages) throws IOException {
        SkyscraperDvbReceiver dvbReceiver = new SkyscraperDvbReceiver();
        DvbContext dvbContext = new DvbContext();
        dvbContext.setDvbReceiver(dvbReceiver);
        byte[] buffer = new byte[188];
        for (long i = 0; i < numPackages; i++)
        {
            if (is.read(buffer,0,188) != 188)
                throw new IOException("incomplete read");
            dvbContext.pushPacket(buffer);
        }

        return dvbReceiver.getSatellites();
    }
}
