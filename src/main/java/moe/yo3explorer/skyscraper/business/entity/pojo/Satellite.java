package moe.yo3explorer.skyscraper.business.entity.pojo;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Satellite {
    public double orbitalPosition;
    public char cardinalDirection;
    public String name;
    private List<Transponder> transponders;

    public void addTransponder(Transponder transponder)
    {
        if (transponders == null)
            transponders = new LinkedList<>();

        Optional<Transponder> first = transponders.stream()
                .filter(x -> x.transportStreamId == transponder.transportStreamId)
                .findFirst();

        if (first.isPresent())
            return;

        transponders.add(transponder);
    }

    @Override
    public String toString() {
        return "Satellite{" +
                "orbitalPosition=" + orbitalPosition +
                ", cardinalDirection=" + cardinalDirection +
                ", name='" + name + '\'' +
                '}';
    }
}
