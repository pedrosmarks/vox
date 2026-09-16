package br.com.fai.Vox.implementation.dao.dashboard;

import br.com.fai.Vox.domain.dto.dashboard.DashboardOverviewDto;
import br.com.fai.Vox.domain.dto.dashboard.DateRangeFilter;
import br.com.fai.Vox.domain.dto.dashboard.HotspotDetailDto;
import br.com.fai.Vox.domain.dto.dashboard.MapPointDto;
import br.com.fai.Vox.domain.dto.dashboard.ModerationHealthDto;
import br.com.fai.Vox.domain.dto.dashboard.NeighborhoodHotspotDto;
import br.com.fai.Vox.port.dao.dashboard.DashboardDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardPostgresDaoImpl implements DashboardDao {

    private final Connection connection;

    public DashboardPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    // =========================================================
    // OVERVIEW / KPIs
    // =========================================================

    @Override
    public DashboardOverviewDto getOverview(int municipalityId, DateRangeFilter dateRange) {
        DashboardOverviewDto dto = new DashboardOverviewDto();

        Map<String, Long> issuesByStatus = countGroupBy(
                "SELECT status::text AS k, COUNT(*) AS c FROM issue_report WHERE municipality_id = ?"
                        + dateClause("created_at", dateRange) + " GROUP BY status",
                municipalityId, dateRange);
        Map<String, Long> issuesByModeration = countGroupBy(
                "SELECT moderation_status::text AS k, COUNT(*) AS c FROM issue_report WHERE municipality_id = ?"
                        + dateClause("created_at", dateRange) + " GROUP BY moderation_status",
                municipalityId, dateRange);
        Map<String, Long> projectsByStatus = countGroupBy(
                "SELECT status::text AS k, COUNT(*) AS c FROM project WHERE municipality_id = ?"
                        + dateClause("created_at", dateRange) + " GROUP BY status",
                municipalityId, dateRange);
        Map<String, Long> projectsByModeration = countGroupBy(
                "SELECT moderation_status::text AS k, COUNT(*) AS c FROM project WHERE municipality_id = ?"
                        + dateClause("created_at", dateRange) + " GROUP BY moderation_status",
                municipalityId, dateRange);
        Map<String, Long> usersByRole = countGroupBy(
                "SELECT role::text AS k, COUNT(*) AS c FROM user_model WHERE municipality_id = ?"
                        + dateClause("created_at", dateRange) + " GROUP BY role",
                municipalityId, dateRange);

        dto.setIssuesByStatus(issuesByStatus);
        dto.setIssuesByModeration(issuesByModeration);
        dto.setProjectsByStatus(projectsByStatus);
        dto.setProjectsByModeration(projectsByModeration);
        dto.setUsersByRole(usersByRole);

        dto.setTotalIssues(sumValues(issuesByStatus));
        dto.setApprovedIssues(issuesByModeration.getOrDefault("APPROVED", 0L));
        dto.setPendingModerationIssues(issuesByModeration.getOrDefault("PENDING", 0L));
        dto.setResolvedIssues(issuesByStatus.getOrDefault("RESOLVED", 0L));

        dto.setTotalProjects(sumValues(projectsByStatus));
        dto.setPublishedProjects(projectsByStatus.getOrDefault("PUBLISHED", 0L));
        dto.setPendingModerationProjects(projectsByModeration.getOrDefault("PENDING", 0L));

        dto.setTotalUsers(sumValues(usersByRole));

        long approved = dto.getApprovedIssues();
        dto.setIssueResolutionRate(approved > 0
                ? round1((dto.getResolvedIssues() * 100.0) / approved)
                : 0.0);

        return dto;
    }

    // =========================================================
    // MAPA - ZONAS QUENTES POR COORDENADA
    // =========================================================

    @Override
    public List<MapPointDto> getMapHotspots(int municipalityId, int precision, DateRangeFilter dateRange) {
        // Quantiza as coordenadas em uma grade arredondando por 'precision' casas.
        // Considera apenas registros aprovados na moderação (visão pública).
        final String sql =
                "SELECT lat, lng, " +
                "       SUM(CASE WHEN src = 'issue' THEN 1 ELSE 0 END) AS issue_count, " +
                "       SUM(CASE WHEN src = 'project' THEN 1 ELSE 0 END) AS project_count " +
                "FROM ( " +
                "   SELECT ROUND(latitude, ?) AS lat, ROUND(longitude, ?) AS lng, 'issue' AS src " +
                "   FROM issue_report WHERE municipality_id = ? AND moderation_status = 'APPROVED'" +
                        dateClause("created_at", dateRange) + " " +
                "   UNION ALL " +
                "   SELECT ROUND(latitude, ?) AS lat, ROUND(longitude, ?) AS lng, 'project' AS src " +
                "   FROM project WHERE municipality_id = ? AND moderation_status = 'APPROVED'" +
                        dateClause("created_at", dateRange) + " " +
                ") grid " +
                "GROUP BY lat, lng " +
                "ORDER BY (issue_count + project_count) DESC";

        final List<MapPointDto> points = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, precision);
            ps.setInt(idx++, precision);
            ps.setInt(idx++, municipalityId);
            idx = bindDateRange(ps, idx, dateRange);
            ps.setInt(idx++, precision);
            ps.setInt(idx++, precision);
            ps.setInt(idx++, municipalityId);
            idx = bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    points.add(new MapPointDto(
                            rs.getBigDecimal("lat"),
                            rs.getBigDecimal("lng"),
                            rs.getLong("issue_count"),
                            rs.getLong("project_count")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return points;
    }

    // =========================================================
    // MAPA - ZONAS QUENTES POR BAIRRO
    // =========================================================

    @Override
    public List<NeighborhoodHotspotDto> getNeighborhoodHotspots(int municipalityId, DateRangeFilter dateRange) {
        final String sql =
                "SELECT neighborhood, " +
                "       SUM(CASE WHEN src = 'issue' THEN 1 ELSE 0 END) AS issue_count, " +
                "       SUM(CASE WHEN src = 'project' THEN 1 ELSE 0 END) AS project_count " +
                "FROM ( " +
                "   SELECT COALESCE(NULLIF(TRIM(neighborhood), ''), 'Não informado') AS neighborhood, 'issue' AS src " +
                "   FROM issue_report WHERE municipality_id = ? AND moderation_status = 'APPROVED'" +
                        dateClause("created_at", dateRange) + " " +
                "   UNION ALL " +
                "   SELECT COALESCE(NULLIF(TRIM(neighborhood), ''), 'Não informado') AS neighborhood, 'project' AS src " +
                "   FROM project WHERE municipality_id = ? AND moderation_status = 'APPROVED'" +
                        dateClause("created_at", dateRange) + " " +
                ") agg " +
                "GROUP BY neighborhood " +
                "ORDER BY (issue_count + project_count) DESC";

        final List<NeighborhoodHotspotDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            idx = bindDateRange(ps, idx, dateRange);
            ps.setInt(idx++, municipalityId);
            idx = bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new NeighborhoodHotspotDto(
                            rs.getString("neighborhood"),
                            rs.getLong("issue_count"),
                            rs.getLong("project_count")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public HotspotDetailDto getNeighborhoodDetail(int municipalityId, String neighborhood, DateRangeFilter dateRange) {
        List<HotspotDetailDto.HotspotItemDto> issues = fetchDetailItems(
                "SELECT i.id, i.title, i.status::text AS status, c.name AS category, i.neighborhood " +
                "FROM issue_report i LEFT JOIN category c ON c.id = i.category_id " +
                "WHERE i.municipality_id = ? AND i.moderation_status = 'APPROVED' " +
                "AND COALESCE(NULLIF(TRIM(i.neighborhood), ''), 'Não informado') = ?" +
                        dateClause("i.created_at", dateRange) + " " +
                "ORDER BY i.created_at DESC",
                municipalityId, neighborhood, dateRange);

        List<HotspotDetailDto.HotspotItemDto> projects = fetchDetailItems(
                "SELECT p.id, p.title, p.status::text AS status, c.name AS category, p.neighborhood " +
                "FROM project p LEFT JOIN category c ON c.id = p.category_id " +
                "WHERE p.municipality_id = ? AND p.moderation_status = 'APPROVED' " +
                "AND COALESCE(NULLIF(TRIM(p.neighborhood), ''), 'Não informado') = ?" +
                        dateClause("p.created_at", dateRange) + " " +
                "ORDER BY p.created_at DESC",
                municipalityId, neighborhood, dateRange);

        return new HotspotDetailDto(issues, projects);
    }

    // =========================================================
    // SAÚDE DA MODERAÇÃO
    // =========================================================

    @Override
    public ModerationHealthDto getModerationHealth(int municipalityId, DateRangeFilter dateRange) {
        ModerationHealthDto dto = new ModerationHealthDto();

        dto.setPendingIssues(scalarCount(
                "SELECT COUNT(*) FROM issue_report WHERE municipality_id = ? AND moderation_status = 'PENDING'"
                        + dateClause("created_at", dateRange),
                municipalityId, dateRange));
        dto.setPendingProjects(scalarCount(
                "SELECT COUNT(*) FROM project WHERE municipality_id = ? AND moderation_status = 'PENDING'"
                        + dateClause("created_at", dateRange),
                municipalityId, dateRange));

        dto.setApprovedIssues(scalarCount(
                "SELECT COUNT(*) FROM issue_moderation m JOIN issue_report i ON i.id = m.issue_id " +
                "WHERE i.municipality_id = ? AND m.action = 'APPROVED'"
                        + dateClause("m.created_at", dateRange),
                municipalityId, dateRange));
        dto.setRejectedIssues(scalarCount(
                "SELECT COUNT(*) FROM issue_moderation m JOIN issue_report i ON i.id = m.issue_id " +
                "WHERE i.municipality_id = ? AND m.action = 'REJECTED'"
                        + dateClause("m.created_at", dateRange),
                municipalityId, dateRange));
        dto.setApprovedProjects(scalarCount(
                "SELECT COUNT(*) FROM project_moderation m JOIN project p ON p.id = m.project_id " +
                "WHERE p.municipality_id = ? AND m.action = 'APPROVED'"
                        + dateClause("m.created_at", dateRange),
                municipalityId, dateRange));
        dto.setRejectedProjects(scalarCount(
                "SELECT COUNT(*) FROM project_moderation m JOIN project p ON p.id = m.project_id " +
                "WHERE p.municipality_id = ? AND m.action = 'REJECTED'"
                        + dateClause("m.created_at", dateRange),
                municipalityId, dateRange));

        long issueDecisions = dto.getApprovedIssues() + dto.getRejectedIssues();
        dto.setIssueApprovalRate(issueDecisions > 0
                ? round1((dto.getApprovedIssues() * 100.0) / issueDecisions) : 0.0);
        long projectDecisions = dto.getApprovedProjects() + dto.getRejectedProjects();
        dto.setProjectApprovalRate(projectDecisions > 0
                ? round1((dto.getApprovedProjects() * 100.0) / projectDecisions) : 0.0);

        dto.setAvgIssueDecisionHours(avgDecisionHours(
                "SELECT AVG(EXTRACT(EPOCH FROM (m.created_at - i.created_at)) / 3600.0) " +
                "FROM issue_moderation m JOIN issue_report i ON i.id = m.issue_id " +
                "WHERE i.municipality_id = ?"
                        + dateClause("m.created_at", dateRange),
                municipalityId, dateRange));
        dto.setAvgProjectDecisionHours(avgDecisionHours(
                "SELECT AVG(EXTRACT(EPOCH FROM (m.created_at - p.created_at)) / 3600.0) " +
                "FROM project_moderation m JOIN project p ON p.id = m.project_id " +
                "WHERE p.municipality_id = ?"
                        + dateClause("m.created_at", dateRange),
                municipalityId, dateRange));

        dto.setTopModerators(fetchTopModerators(municipalityId, dateRange));
        return dto;
    }

    private List<ModerationHealthDto.ModeratorStatDto> fetchTopModerators(int municipalityId, DateRangeFilter dateRange) {
        // Une decisões de issues e projetos por moderador, escopado ao município.
        final String sql =
                "SELECT u.id AS moderator_id, u.name AS moderator_name, COUNT(*) AS decisions FROM ( " +
                "   SELECT m.moderator_id FROM issue_moderation m JOIN issue_report i ON i.id = m.issue_id " +
                "   WHERE i.municipality_id = ? AND m.moderator_id IS NOT NULL" +
                        dateClause("m.created_at", dateRange) + " " +
                "   UNION ALL " +
                "   SELECT m.moderator_id FROM project_moderation m JOIN project p ON p.id = m.project_id " +
                "   WHERE p.municipality_id = ? AND m.moderator_id IS NOT NULL" +
                        dateClause("m.created_at", dateRange) + " " +
                ") d JOIN user_model u ON u.id = d.moderator_id " +
                "GROUP BY u.id, u.name " +
                "ORDER BY decisions DESC LIMIT 10";

        final List<ModerationHealthDto.ModeratorStatDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            idx = bindDateRange(ps, idx, dateRange);
            ps.setInt(idx++, municipalityId);
            idx = bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ModerationHealthDto.ModeratorStatDto(
                            rs.getInt("moderator_id"),
                            rs.getString("moderator_name"),
                            rs.getLong("decisions")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    /**
     * Monta a cláusula SQL de intervalo de datas (com AND inicial) para a coluna
     * informada. Retorna string vazia quando não há limites definidos.
     */
    private static String dateClause(String column, DateRangeFilter dateRange) {
        if (dateRange == null || !dateRange.hasAnyBound()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (dateRange.hasFrom()) {
            sb.append(" AND ").append(column).append(" >= ?");
        }
        if (dateRange.hasTo()) {
            sb.append(" AND ").append(column).append(" <= ?");
        }
        return sb.toString();
    }

    /**
     * Vincula os parâmetros do intervalo de datas na ordem em que
     * {@link #dateClause(String, DateRangeFilter)} os adicionou.
     *
     * @return o próximo índice de parâmetro disponível
     */
    private static int bindDateRange(PreparedStatement ps, int startIndex, DateRangeFilter dateRange) throws SQLException {
        int idx = startIndex;
        if (dateRange == null || !dateRange.hasAnyBound()) {
            return idx;
        }
        if (dateRange.hasFrom()) {
            ps.setTimestamp(idx++, Timestamp.valueOf(dateRange.getFrom()));
        }
        if (dateRange.hasTo()) {
            ps.setTimestamp(idx++, Timestamp.valueOf(dateRange.getTo()));
        }
        return idx;
    }

    private Map<String, Long> countGroupBy(String sql, int municipalityId, DateRangeFilter dateRange) {
        final Map<String, Long> result = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("k"), rs.getLong("c"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private long scalarCount(String sql, int municipalityId, DateRangeFilter dateRange) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Double avgDecisionHours(String sql, int municipalityId, DateRangeFilter dateRange) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double v = rs.getDouble(1);
                    if (rs.wasNull()) return null;
                    return round1(v);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private List<HotspotDetailDto.HotspotItemDto> fetchDetailItems(String sql,
                                                                   int municipalityId,
                                                                   String neighborhood,
                                                                   DateRangeFilter dateRange) {
        final List<HotspotDetailDto.HotspotItemDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            ps.setString(idx++, neighborhood);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HotspotDetailDto.HotspotItemDto(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("status"),
                            rs.getString("category"),
                            rs.getString("neighborhood")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private static long sumValues(Map<String, Long> map) {
        long total = 0;
        for (long v : map.values()) total += v;
        return total;
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
