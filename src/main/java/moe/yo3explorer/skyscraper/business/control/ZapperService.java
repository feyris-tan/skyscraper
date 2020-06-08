package moe.yo3explorer.skyscraper.business.control;

import moe.yo3explorer.dvb4j.model.enums.ModulationType;
import moe.yo3explorer.dvb4j.model.enums.Polarization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ZapperService {

    private static final int ZAPPING_DURATION = 60;

    private Logger logger;
    public ZapperService()
    {
        logger = LogManager.getLogger(getClass());
    }

    public File tryZapTo(int diseqc, double frequency, long symbolrate, @NotNull Polarization polarization, boolean s2, ModulationType modulationType)
    {
        try {
            return zapTo(diseqc, frequency, symbolrate, polarization, s2, modulationType);
        } catch (IOException | InterruptedException e) {
            throw new ZappingException(e);
        }
    }

    public File findTsDuck()
    {
        File tsDuckWin64 = new File("C:\\Program Files\\TSDuck\\bin\\tsp.exe");
        if (tsDuckWin64.isFile())
            return tsDuckWin64;
        File tsDuckUbuntu = new File ("/usr/bin/tsp");
        if (tsDuckUbuntu.isFile())
            return tsDuckUbuntu;
        return null;
    }

    @Contract(pure = true)
    private @NotNull String getDuckModulation(@NotNull ModulationType modulationType)
    {
        switch (modulationType)
        {
            case QPSK:
                return "QPSK";
            case _8PSK:
                return "8-PSK";
            case _16QAM:
                return "16-QAM";
            default:
                throw new RuntimeException("don't know what this is...");
        }
    }

    public File duckTo(@NotNull File tsDuckExe, int diseqc, double frequency, long symbolrate, @NotNull Polarization polarization, boolean s2, ModulationType modulationType) throws IOException, InterruptedException {
        File tsFile = new File("test2.ts");
        ProcessBuilder processBuilder = new ProcessBuilder();
        // .\tsp -v -I dvb -a 0 --satellite-number 0 --lnb Extended
        // --delivery-system DVB-S2 --frequency 10714000000
        // --symbol-rate 22000 --polarity horizontal -P until -s 10
        // -O file C:\Temp\test.ts

        long duckFrequency = (long)(frequency * 1000000f);
        long duckSymbolRate = symbolrate * 1000;
        ProcessBuilder duckBuilder = processBuilder.command(
                tsDuckExe.getAbsolutePath(),
                "-v",           //Zeit nachdem die Aufzeichnung gestoppt werden soll
                "-I",
                "dvb",
                "-a",
                "0",
                "--satellite-number",
                Integer.toString(diseqc),
                "--lnb",
                "Extended",
                "--delivery-system",
                s2 ? "DVB-S2" : "DVB-S",
                "--frequency",
                Long.toString(duckFrequency),
                "--symbol-rate",
                Long.toString(duckSymbolRate),
                "--polarity",
                polarization.toString().toLowerCase(),
                "--modulation",
                getDuckModulation(modulationType),
                "-P",
                "until",
                "-s",
                Integer.toString(ZAPPING_DURATION),
                "-O",
                "file",
                tsFile.getName()
        );
        Process duckProc = duckBuilder.start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(duckProc.getErrorStream()));
        while (true)
        {
            String line = bufferedReader.readLine();
            if (line == null)
                break;
            logger.info(line);
        }
        duckProc.waitFor();
        return tsFile;
    }

    public File zapTo(int diseqc, double frequency, long symbolrate, @NotNull Polarization polarization, boolean s2, ModulationType modulationType) throws IOException, InterruptedException {
        File tsFile = new File("test2.ts");
        File cnfFile = new File("test2.cnf");
        UUID uuid = UUID.randomUUID();

        if (cnfFile.isFile())
            cnfFile.delete();

        if (tsFile.exists())
            tsFile.delete();

        File tsDuck = findTsDuck();
        if (tsDuck != null)
        {
            return duckTo(tsDuck, diseqc, frequency, symbolrate, polarization, s2, modulationType);
        }

        throw new RuntimeException("no working tsduck found.");
    }


}
