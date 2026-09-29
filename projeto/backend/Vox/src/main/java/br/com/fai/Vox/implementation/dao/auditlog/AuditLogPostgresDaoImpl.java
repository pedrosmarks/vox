package br.com.fai.Vox.implementation.dao.auditlog;

import br.com.fai.Vox.domain.AuditLog;
import br.com.fai.Vox.port.dao.auditlog.AuditLogDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuditLogPostgresDaoImpl implements AuditLogDao {

    private static final Logger logger = Logger.getLogger(AuditLogPostgresDaoImpl.class.getName());

    private final Connection connection;

    public AuditLogPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(AuditLog e) {
        final String sql = "INSERT INTO audit_log " +
                "(user_id, user_role, municipality_id, http_method, path, query_string, " +
                "status_code, success, duration_ms, ip_address, user_agent, error_message) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
            logger.log(Level.WARNING, "Falha ao gravar audit_log", ex);
        }
    }

    @Override
    public List<AuditLog> find(Integer municipalityId, Integer userId, String httpMethod,
                               LocalDateTime from, LocalDateTime to, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, municipalityId, userId, httpMethod, from, to);
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        final List<AuditLog> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return list;
    }

    @Override
    public long count(Integer municipalityId, Integer userId, String httpMethod,
                      LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, municipalityId, userId, httpMethod, from, to);

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void appendFilters(StringBuilder sql, List<Object> params, Integer municipalityId,
                               Integer userId, String httpMethod, LocalDateTime from, LocalDateTime to) {
        if (municipalityId != null) {
            sql.append(" AND municipality_id = ?");
            params.add(municipalityId);
        }
        if (userId != null) {
            sql.append(" AND user_id = ?");
            params.add(userId);
        }
        if (httpMethod != null && !httpMethod.isBlank()) {
            sql.append(" AND http_method = ?");
            params.add(httpMethod.trim().toUpperCase());
        }
        if (from != null) {
            sql.append(" AND created_at >= ?");
            params.add(Timestamp.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND created_at <= ?");
            params.add(Timestamp.valueOf(to));
        }
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object p = params.get(i);
            if (p instanceof Integer) ps.setInt(i + 1, (Integer) p);
            else if (p instanceof Long) ps.setLong(i + 1, (Long) p);
            else if (p instanceof Timestamp) ps.setTimestamp(i + 1, (Timestamp) p);
            else ps.setString(i + 1, (String) p);
        }
    }

    private AuditLog mapResultSet(ResultSet rs) throws SQLException {
        AuditLog e = new AuditLog();
        e.setId(rs.getLong("id"));
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) e.setUserId(userId);
        e.setUserRole(rs.getString("user_role"));
        int municipalityId = rs.getInt("municipality_id");
        if (!rs.wasNull()) e.setMunicipalityId(municipalityId);
        e.setHttpMethod(rs.getString("http_method"));
        e.setPath(rs.getString("path"));
        e.setQueryString(rs.getString("query_string"));
        e.setStatusCode(rs.getInt("status_code"));
        e.setSuccess(rs.getBoolean("success"));
        long durationMs = rs.getLong("duration_ms");
        if (!rs.wasNull()) e.setDurationMs(durationMs);
        e.setIpAddress(rs.getString("ip_address"));
        e.setUserAgent(rs.getString("user_agent"));
        e.setErrorMessage(rs.getString("error_message"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) e.setCreatedAt(createdAt.toLocalDateTime());
        return e;
    }
}
