package moe.yo3explorer.skyscraper.presentation;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import moe.yo3explorer.skyscraper.business.boundary.SkyscraperService;

import javax.inject.Inject;
import java.io.File;

public class SkyscraperQuarkusApplication implements QuarkusApplication {
    @Inject
    SkyscraperService skyscraperService;

    @Override
    public int run(String... args) throws Exception {
        File test = new File("E:\\11494_H_22000.ts");
        skyscraperService.scrapeFile(test);
        return 0;
    }
}
