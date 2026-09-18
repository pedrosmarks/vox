package br.com.fai.Vox.port.dao.auditlog;

import br.com.fai.Vox.domain.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Persistência dos registros de auditoria. Consultas são escopadas por
 * município (visão do administrador).
 */
public interface AuditLogDao {

    void create(AuditLog entity);

    /**
     * Consulta paginada com filtros opcionais.
     *
     * @param municipalityId escopo obrigatório (município do admin)
     * @param userId         filtra por usuário (opcional)
     * @param httpMethod     filtra por método HTTP (opcional)
     * @param from           limite inferior de created_at (opcional)
     * @param to             limite superior de created_at (opcional)
     */
    List<AuditLog> find(Integer municipalityId, Integer userId, String httpMethod,
                        LocalDateTime from, LocalDateTime to, int limit, int offset);

    long count(Integer municipalityId, Integer userId, String httpMethod,
               LocalDateTime from, LocalDateTime to);
}
