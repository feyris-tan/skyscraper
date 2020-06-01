package moe.yo3explorer.skyscraper.business.boundary;

import moe.yo3explorer.skyscraper.business.entity.SatelliteEntity;
import moe.yo3explorer.skyscraper.business.entity.TransponderEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class LibrarianService
{
    private Logger logger;
    private boolean enable;
    private String path;
    private File outputPath;

    public LibrarianService() throws IOException {
        logger = LogManager.getLogger(getClass());

        File propFile = new File("librarian.properties");
        if (!propFile.isFile()) {
            logger.info("Librarian properties missing!");
            return;
        }

        FileInputStream fis = new FileInputStream(propFile);
        Properties properties = new Properties();
        properties.load(fis);
        fis.close();

        if (!properties.containsKey("enabled")) {
            logger.info("no enabled property!");
            return;
        }
        if (!properties.containsKey("path")) {
            logger.info("No path property!");
            return;
        }

        String enabled = properties.getProperty("enabled");
        if (enabled.equals("0")) {
            logger.info("enabled is zero!");
            return;
        }

        path = properties.getProperty("path");
        outputPath = new File(path);
        if (!outputPath.exists())
            outputPath.mkdir();
        enable = true;
    }


    public void catalogue(@NotNull SatelliteEntity satelliteEntity, @NotNull TransponderEntity transponderEntity, File file) throws IOException {
        if (!enable)
            return;

        int intFrequency = (int)transponderEntity.frequency;
        String strPolarity = transponderEntity.polarization.toString().substring(0,1);

        String fname = String.format("%s_%d_%s_%d.ts",satelliteEntity.name,intFrequency,strPolarity,transponderEntity.symbolrate);
        Path of = Path.of(outputPath.getAbsolutePath(), fname);
        File outFile = of.toFile();
        if (outFile.isFile()) {
            logger.info("Is already in library!");
            return;
        }

        logger.info("Ingesting into library!");
        Files.copy(file.toPath(),outFile.toPath());
        logger.info("Done copying into library!");
    }
}
