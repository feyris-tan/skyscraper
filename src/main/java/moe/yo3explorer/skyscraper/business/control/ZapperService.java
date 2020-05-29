package moe.yo3explorer.skyscraper.business.control;

import moe.yo3explorer.dvb4j.model.enums.Polarization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ZapperService {

    private Logger logger;
    public ZapperService()
    {
        logger = LogManager.getLogger(getClass());
    }

    public boolean isAvailable()
    {
        File frontend = new File("/dev/dvb/adapter0");
        boolean result = frontend.exists();
        return result;
    }

    public File tryZapTo(int diseqc, double frequency, long symbolrate, @NotNull Polarization polarization, boolean s2)
    {
        try {
            return zapTo(diseqc, frequency, symbolrate, polarization, s2);
        } catch (IOException | InterruptedException e) {
            throw new ZappingException(e);
        }
    }

    public File zapTo(int diseqc, double frequency, long symbolrate, @NotNull Polarization polarization, boolean s2) throws IOException, InterruptedException {
        File tsFile = new File("test2.ts");
        File cnfFile = new File("test2.cnf");
        UUID uuid = UUID.randomUUID();

        long rawFrequency = (long)(frequency * 1000.0);
        symbolrate *= 1000;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("[%s]\n",uuid.toString()));
        stringBuilder.append(String.format("\tLNB = EXTENDED\n"));
        stringBuilder.append(String.format("\tFREQUENCY = %d\n",rawFrequency));
        stringBuilder.append(String.format("\tSYMBOL_RATE = %d\n",symbolrate));
        stringBuilder.append(String.format("\tPOLARIZATION = %s\n",polarization.toString()));
        stringBuilder.append(String.format("\tDELIVERY_SYSTEM = %s\n",s2 ? "DVBS2" : "DVBS"));

        if (cnfFile.isFile())
            cnfFile.delete();
        FileWriter fileWriter = new FileWriter(cnfFile, StandardCharsets.UTF_8,false);
        fileWriter.write(stringBuilder.toString());
        fileWriter.flush();
        fileWriter.close();

        if (tsFile.exists())
            tsFile.delete();

        ProcessBuilder processBuilder = new ProcessBuilder();
        // dvbv5-zap -t 60 -S 0 -P -o test3.ts -N -c test.cnf jeffinator
        ProcessBuilder zapBuilder = processBuilder.command(
                "dvbv5-zap",
                "-t",           //Zeit nachdem die Aufzeichnung gestoppt werden soll
                "60",
                "-S",           //DiseqC-Position
                Integer.toString(diseqc),
                "-P",           //Alle PIDs aufzeichnen
                "-o",           //Aufzeichnen
                tsFile.getAbsolutePath(),
                "-N",           //Maschinenlesbarer Output
                "-c",           //Pfad zur Datei
                cnfFile.getAbsolutePath(),
                uuid.toString() //Kanalname
        );
        Process zapProc = zapBuilder.start();


        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zapProc.getErrorStream()));
        int mistakes = 0;
        while (true)
        {
            String line = bufferedReader.readLine();
            if (line == null)
                break;
            if (line.startsWith("status "))
            {
                logger.info(line);
                if (!line.contains("HAS_LOCK"))
                    mistakes++;
            }
            if (mistakes == 10)
            {
                zapProc.destroy();
                break;
            }
        }
        zapProc.waitFor();
        return tsFile;
    }


}
