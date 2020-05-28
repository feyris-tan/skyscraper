package moe.yo3explorer.skyscraper.business.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import moe.yo3explorer.dvb4j.model.enums.FEC;
import moe.yo3explorer.dvb4j.model.enums.ModulationType;
import moe.yo3explorer.dvb4j.model.enums.Polarization;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Table(name = "transponders", catalog = "skyscraper")
@Entity
public class TransponderEntity extends PanacheEntityBase {
    @Id
    public int id;
    @CreationTimestamp
    public Date dateadded;
    public double frequency;
    @Enumerated(EnumType.STRING)
    public Polarization polarization;
    public int symbolrate;
    @Enumerated(EnumType.STRING)
    public FEC fec;
    @Enumerated(EnumType.STRING)
    public ModulationType modulation;
    public boolean s2;
    public int satellite;
    public int network;
    public int transportstream;
    public Date lastscanned;
    public Date lastvalid;
}
