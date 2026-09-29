package br.com.fai.Vox.port.dao.auditlog;

import br.com.fai.Vox.domain.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogDao {

    void create(AuditLog entity);

    List<AuditLog> find(Integer municipalityId, Integer userId, String httpMethod,
                        LocalDateTime from, LocalDateTime to, int limit, int offset);

    long count(Integer municipalityId, Integer userId, String httpMethod,
               LocalDateTime from, LocalDateTime to);
}
