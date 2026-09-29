package br.com.fai.Vox.configuration.audit;

import br.com.fai.Vox.port.service.auditlog.AuditLogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuditRetentionJob {

    private static final int RETENTION_DAYS = 90;

    private final AuditLogService auditLogService;

    public AuditRetentionJob(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void purgeOldLogs() {
        auditLogService.purgeOlderThan(RETENTION_DAYS);
    }
}
