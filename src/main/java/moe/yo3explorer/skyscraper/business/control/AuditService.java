package moe.yo3explorer.skyscraper.business.control;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import moe.yo3explorer.skyscraper.business.entity.AuditEntity;
import moe.yo3explorer.skyscraper.business.entity.AuditOperation;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Singleton
public class AuditService
{
    @Transactional(Transactional.TxType.MANDATORY)
    public void performAudit(AuditOperation operation, @org.jetbrains.annotations.NotNull Class<? extends PanacheEntityBase> entityType, String text)
    {
        AuditEntity audit = new AuditEntity();
        audit.operation = operation;
        audit.entitytype = entityType.getSimpleName();
        audit.text = text;
        audit.persist();
    }
}
