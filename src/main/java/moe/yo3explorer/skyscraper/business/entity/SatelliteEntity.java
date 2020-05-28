package moe.yo3explorer.skyscraper.business.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "satellites",schema = "skyscraper")
public class SatelliteEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;
    @CreationTimestamp
    public Date dateadded;
    public double orbitalposition;
    public String cardinalposition;
    public String name;
    public Integer diseqc;
}
