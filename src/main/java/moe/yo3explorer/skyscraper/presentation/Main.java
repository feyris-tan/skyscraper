package moe.yo3explorer.skyscraper.presentation;

import moe.yo3explorer.skyscraper.business.boundary.SkyscraperService;

import java.io.IOException;
import java.sql.SQLException;

public class Main
{
    public static void main(String[] args) throws IOException, SQLException {
        SkyscraperService skyscraperService = new SkyscraperService();
        skyscraperService.perform();
    }
}
