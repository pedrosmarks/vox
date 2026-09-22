package br.com.fai.Vox.implementation.service.auditlog;

import br.com.fai.Vox.domain.AuditLog;
import br.com.fai.Vox.domain.dto.PageResponse;
import br.com.fai.Vox.port.dao.auditlog.AuditLogDao;
import br.com.fai.Vox.port.service.auditlog.AuditLogService;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger logger = Logger.getLogger(AuditLogServiceImpl.class.getName());

    /**
     * A ESCRITA e o PURGE usam o pool Hikari diretamente (uma conexão própria por
     * operação, via try-with-resources). Isso é obrigatório porque a Connection
     * injetada nos DAOs é um bean singleton compartilhado — usá-la a partir de uma
     * thread assíncrona/agendada geraria condição de corrida no JDBC.
     * A LEITURA (consulta do admin) roda na thread da request e reutiliza o DAO.
     */
    private final HikariDataSource dataSource;
    private final AuditLogDao auditLogDao;

    public AuditLogServiceImpl(HikariDataSource dataSource, AuditLogDao auditLogDao) {
        this.dataSource = dataSource;
        this.auditLogDao = auditLogDao;
    }

    @Async("auditTaskExecutor")
    @Override
    public void record(AuditLog e) {
        final String sql = "INSERT INTO audit_log " +
                "(user_id, user_role, municipality_id, http_method, path, query_string, " +
                "status_code, success, duration_ms, ip_address, user_agent, error_message) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (e.getUserId() != null) ps.setInt(1, e.getUserId()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, e.getUserRole());
            if (e.getMunicipalityId() != null) ps.setInt(3, e.getMunicipalityId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, e.getHttpMethod());
            ps.setString(5, e.getPath());
            ps.setString(6, e.getQueryString());
            ps.setInt(7, e.getStatusCode());
            ps.setBoolean(8, e.isSuccess());
            if (e.getDurationMs() != null) ps.setLong(9, e.getDurationMs()); else ps.setNull(9, Types.BIGINT);
            ps.setString(10, e.getIpAddress());
            ps.setString(11, e.getUserAgent());
            ps.setString(12, e.getErrorMessage());
            ps.executeUpdate();
        } catch (SQLException ex) {
            // Auditoria nunca deve derrubar o fluxo: apenas registra a falha.
            logger.log(Level.WARNING, "Falha ao gravar audit_log", ex);
        }
    }

    @Override
    public PageResponse<AuditLog> find(int municipalityId, Integer userId, String httpMethod,
                                       LocalDate from, LocalDate to, int page, int size) {
        if (municipalityId <= 0) return new PageResponse<>(List.of(), page, size, 0);
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : Math.min(size, 200);
        int offset = safePage * safeSize;

        LocalDateTime fromDt = from == null ? null : from.atStartOfDay();
        LocalDateTime toDt = to == null ? null : to.atTime(LocalTime.MAX);

        List<AuditLog> content = auditLogDao.find(municipalityId, userId, httpMethod, fromDt, toDt, safeSize, offset);
        long total = auditLogDao.count(municipalityId, userId, httpMethod, fromDt, toDt);
        return new PageResponse<>(content, safePage, safeSize, total);
    }

    @Override
    public int purgeOlderThan(int retentionDays) {
        if (retentionDays <= 0) return 0;
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        final String sql = "DELETE FROM audit_log WHERE created_at < ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(threshold));
            int deleted = ps.executeUpdate();
            logger.log(Level.INFO, "Auditoria: " + deleted + " registro(s) com mais de "
                    + retentionDays + " dias removido(s).");
            return deleted;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Falha ao limpar audit_log antigo", ex);
            return 0;
        }
    }
}
