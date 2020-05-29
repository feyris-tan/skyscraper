package moe.yo3explorer.skyscraper.business.entity;

import moe.yo3explorer.dvb4j.model.enums.FEC;
import moe.yo3explorer.dvb4j.model.enums.ModulationType;
import moe.yo3explorer.dvb4j.model.enums.Polarization;

import java.sql.Date;
import java.sql.Timestamp;

public class TransponderEntity {
    public int id;
    public Timestamp dateadded;
    public double frequency;
    public Polarization polarization;
    public int symbolrate;
    public FEC fec;
    public ModulationType modulation;
    public boolean s2;
    public int satellite;
    public int network;
    public int transportstream;
    public Timestamp lastscanned;
    public Timestamp lastvalid;
}
