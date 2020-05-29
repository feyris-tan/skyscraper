package moe.yo3explorer.skyscraper.business.control;

import java.io.IOException;

public class SkyscraperException extends RuntimeException {
    public SkyscraperException(Exception e) {
        super(e);
    }

    protected SkyscraperException() {}
}
