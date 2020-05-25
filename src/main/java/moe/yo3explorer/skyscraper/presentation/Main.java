package moe.yo3explorer.skyscraper.presentation;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Main
{
    public static void main(String[] args) {
        System.out.println("Time to ascend");
        Quarkus.run(SkyscraperQuarkusApplication.class,args);
    }
}
