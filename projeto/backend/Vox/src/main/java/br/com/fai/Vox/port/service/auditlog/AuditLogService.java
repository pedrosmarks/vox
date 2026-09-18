package br.com.fai.Vox.port.service.auditlog;

import br.com.fai.Vox.domain.AuditLog;
import br.com.fai.Vox.domain.dto.PageResponse;

import java.time.LocalDate;

/**
 * Registro e consulta de auditoria de chamadas.
 *
 * <p>A gravação ({@link #record(AuditLog)}) é assíncrona e tolerante a falhas:
 * nunca deve interromper o fluxo da requisição original.</p>
 */
public interface AuditLogService {

    /** Grava um registro de auditoria de forma assíncrona. */
    void record(AuditLog entity);

    /** Consulta paginada com filtros opcionais, escopada por município. */
    PageResponse<AuditLog> find(int municipalityId, Integer userId, String httpMethod,
                                LocalDate from, LocalDate to, int page, int size);

    /** Remove registros mais antigos que {@code retentionDays} dias. Retorna quantos foram removidos. */
    int purgeOlderThan(int retentionDays);
}
