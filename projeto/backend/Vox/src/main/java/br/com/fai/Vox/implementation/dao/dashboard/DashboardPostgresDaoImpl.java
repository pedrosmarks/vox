package br.com.fai.Vox.implementation.dao.dashboard;

import br.com.fai.Vox.domain.dto.dashboard.DashboardOverviewDto;
import br.com.fai.Vox.domain.dto.dashboard.HotspotDetailDto;
import br.com.fai.Vox.domain.dto.dashboard.MapPointDto;
import br.com.fai.Vox.domain.dto.dashboard.ModerationHealthDto;
import br.com.fai.Vox.domain.dto.dashboard.NeighborhoodHotspotDto;
import br.com.fai.Vox.port.dao.dashboard.DashboardDao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public DashboardOverviewDto getOverview(int municipalityId) {
        DashboardOverviewDto dto = new DashboardOverviewDto();

        Map<String, Long> issuesByStatus = countGroupBy(
                "SELECT status::text AS k, COUNT(*) AS c FROM issue_report WHERE municipality_id = ? GROUP BY status",
                municipalityId);
        Map<String, Long> issuesByModeration = countGroupBy(
                "SELECT moderation_status::text AS k, COUNT(*) AS c FROM issue_report WHERE municipality_id = ? GROUP BY moderation_status",
                municipalityId);
        Map<String, Long> projectsByStatus = countGroupBy(
                "SELECT status::text AS k, COUNT(*) AS c FROM project WHERE municipality_id = ? GROUP BY status",
                municipalityId);
        Map<String, Long> projectsByModeration = countGroupBy(
                "SELECT moderation_status::text AS k, COUNT(*) AS c FROM project WHERE municipality_id = ? GROUP BY moderation_status",
                municipalityId);
        Map<String, Long> usersByRole = countGroupBy(
                "SELECT role::text AS k, COUNT(*) AS c FROM user_model WHERE municipality_id = ? GROUP BY role",
                municipalityId);

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
    public List<MapPointDto> getMapHotspots(int municipalityId, int precision) {
        // Quantiza as coordenadas em uma grade arredondando por 'precision' casas.
        // Considera apenas registros aprovados na moderação (visão pública).
        final String sql =
                "SELECT lat, lng, " +
                "       SUM(CASE WHEN src = 'issue' THEN 1 ELSE 0 END) AS issue_count, " +
                "       SUM(CASE WHEN src = 'project' THEN 1 ELSE 0 END) AS project_count " +
                "FROM ( " +
                "   SELECT ROUND(latitude, ?) AS lat, ROUND(longitude, ?) AS lng, 'issue' AS src " +
                "   FROM issue_report WHERE municipality_id = ? AND moderation_status = 'APPROVED' " +
                "   UNION ALL " +
                "   SELECT ROUND(latitude, ?) AS lat, ROUND(longitude, ?) AS lng, 'project' AS src " +
                "   FROM project WHERE municipality_id = ? AND moderation_status = 'APPROVED' " +
                ") grid " +
                "GROUP BY lat, lng " +
                "ORDER BY (issue_count + project_count) DESC";

        final List<MapPointDto> points = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, precision);
            ps.setInt(2, precision);
            ps.setInt(3, municipalityId);
            ps.setInt(4, precision);
            ps.setInt(5, precision);
            ps.setInt(6, municipalityId);
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
    public List<NeighborhoodHotspotDto> getNeighborhoodHotspots(int municipalityId) {
        final String sql =
                "SELECT neighborhood, " +
                "       SUM(CASE WHEN src = 'issue' THEN 1 ELSE 0 END) AS issue_count, " +
                "       SUM(CASE WHEN src = 'project' THEN 1 ELSE 0 END) AS project_count " +
                "FROM ( " +
                "   SELECT COALESCE(NULLIF(TRIM(neighborhood), ''), 'Não informado') AS neighborhood, 'issue' AS src " +
                "   FROM issue_report WHERE municipality_id = ? AND moderation_status = 'APPROVED' " +
                "   UNION ALL " +
                "   SELECT COALESCE(NULLIF(TRIM(neighborhood), ''), 'Não informado') AS neighborhood, 'project' AS src " +
                "   FROM project WHERE municipality_id = ? AND moderation_status = 'APPROVED' " +
                ") agg " +
                "GROUP BY neighborhood " +
                "ORDER BY (issue_count + project_count) DESC";

        final List<NeighborhoodHotspotDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, municipalityId);
            ps.setInt(2, municipalityId);
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
    public HotspotDetailDto getNeighborhoodDetail(int municipalityId, String neighborhood) {
        List<HotspotDetailDto.HotspotItemDto> issues = fetchDetailItems(
                "SELECT i.id, i.title, i.status::text AS status, c.name AS category, i.neighborhood " +
                "FROM issue_report i LEFT JOIN category c ON c.id = i.category_id " +
                "WHERE i.municipality_id = ? AND i.moderation_status = 'APPROVED' " +
                "AND COALESCE(NULLIF(TRIM(i.neighborhood), ''), 'Não informado') = ? " +
                "ORDER BY i.created_at DESC",
                municipalityId, neighborhood);

        List<HotspotDetailDto.HotspotItemDto> projects = fetchDetailItems(
                "SELECT p.id, p.title, p.status::text AS status, c.name AS category, p.neighborhood " +
                "FROM project p LEFT JOIN category c ON c.id = p.category_id " +
                "WHERE p.municipality_id = ? AND p.moderation_status = 'APPROVED' " +
                "AND COALESCE(NULLIF(TRIM(p.neighborhood), ''), 'Não informado') = ? " +
                "ORDER BY p.created_at DESC",
                municipalityId, neighborhood);

        return new HotspotDetailDto(issues, projects);
    }

    // =========================================================
    // SAÚDE DA MODERAÇÃO
    // =========================================================

    @Override
    public ModerationHealthDto getModerationHealth(int municipalityId) {
        ModerationHealthDto dto = new ModerationHealthDto();

        dto.setPendingIssues(scalarCount(
                "SELECT COUNT(*) FROM issue_report WHERE municipality_id = ? AND moderation_status = 'PENDING'",
                municipalityId));
        dto.setPendingProjects(scalarCount(
                "SELECT COUNT(*) FROM project WHERE municipality_id = ? AND moderation_status = 'PENDING'",
                municipalityId));

        dto.setApprovedIssues(scalarCount(
                "SELECT COUNT(*) FROM issue_moderation m JOIN issue_report i ON i.id = m.issue_id " +
                "WHERE i.municipality_id = ? AND m.action = 'APPROVED'",
                municipalityId));
        dto.setRejectedIssues(scalarCount(
                "SELECT COUNT(*) FROM issue_moderation m JOIN issue_report i ON i.id = m.issue_id " +
                "WHERE i.municipality_id = ? AND m.action = 'REJECTED'",
                municipalityId));
        dto.setApprovedProjects(scalarCount(
                "SELECT COUNT(*) FROM project_moderation m JOIN project p ON p.id = m.project_id " +
                "WHERE p.municipality_id = ? AND m.action = 'APPROVED'",
                municipalityId));
        dto.setRejectedProjects(scalarCount(
                "SELECT COUNT(*) FROM project_moderation m JOIN project p ON p.id = m.project_id " +
                "WHERE p.municipality_id = ? AND m.action = 'REJECTED'",
                municipalityId));

        long issueDecisions = dto.getApprovedIssues() + dto.getRejectedIssues();
        dto.setIssueApprovalRate(issueDecisions > 0
                ? round1((dto.getApprovedIssues() * 100.0) / issueDecisions) : 0.0);
        long projectDecisions = dto.getApprovedProjects() + dto.getRejectedProjects();
        dto.setProjectApprovalRate(projectDecisions > 0
                ? round1((dto.getApprovedProjects() * 100.0) / projectDecisions) : 0.0);

        dto.setAvgIssueDecisionHours(avgDecisionHours(
                "SELECT AVG(EXTRACT(EPOCH FROM (m.created_at - i.created_at)) / 3600.0) " +
                "FROM issue_moderation m JOIN issue_report i ON i.id = m.issue_id " +
                "WHERE i.municipality_id = ?",
                municipalityId));
        dto.setAvgProjectDecisionHours(avgDecisionHours(
                "SELECT AVG(EXTRACT(EPOCH FROM (m.created_at - p.created_at)) / 3600.0) " +
                "FROM project_moderation m JOIN project p ON p.id = m.project_id " +
                "WHERE p.municipality_id = ?",
                municipalityId));

        dto.setTopModerators(fetchTopModerators(municipalityId));
        return dto;
    }

    private List<ModerationHealthDto.ModeratorStatDto> fetchTopModerators(int municipalityId) {
        // Une decisões de issues e projetos por moderador, escopado ao município.
        final String sql =
                "SELECT u.id AS moderator_id, u.name AS moderator_name, COUNT(*) AS decisions FROM ( " +
                "   SELECT m.moderator_id FROM issue_moderation m JOIN issue_report i ON i.id = m.issue_id " +
                "   WHERE i.municipality_id = ? AND m.moderator_id IS NOT NULL " +
                "   UNION ALL " +
                "   SELECT m.moderator_id FROM project_moderation m JOIN project p ON p.id = m.project_id " +
                "   WHERE p.municipality_id = ? AND m.moderator_id IS NOT NULL " +
                ") d JOIN user_model u ON u.id = d.moderator_id " +
                "GROUP BY u.id, u.name " +
                "ORDER BY decisions DESC LIMIT 10";

        final List<ModerationHealthDto.ModeratorStatDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, municipalityId);
            ps.setInt(2, municipalityId);
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

    private Map<String, Long> countGroupBy(String sql, int municipalityId) {
        final Map<String, Long> result = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, municipalityId);
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

    private long scalarCount(String sql, int municipalityId) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, municipalityId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Double avgDecisionHours(String sql, int municipalityId) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, municipalityId);
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
                                                                   String neighborhood) {
        final List<HotspotDetailDto.HotspotItemDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, municipalityId);
            ps.setString(2, neighborhood);
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
