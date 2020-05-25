package moe.yo3explorer.skyscraper.business.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "audit",schema = "skyscraper")
public class Audit extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    @CreationTimestamp
    public Timestamp dateadded;
    @Enumerated(EnumType.STRING)
    public AuditOperation operation;
    public String entitytype;
    public String text;
}
