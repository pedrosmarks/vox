package br.com.fai.Vox.port.service.auditlog;

import br.com.fai.Vox.domain.AuditLog;
import br.com.fai.Vox.domain.dto.PageResponse;

import java.time.LocalDate;

public interface AuditLogService {

    void record(AuditLog entity);

    PageResponse<AuditLog> find(int municipalityId, Integer userId, String httpMethod,
                                LocalDate from, LocalDate to, int page, int size);

    int purgeOlderThan(int retentionDays);
}
