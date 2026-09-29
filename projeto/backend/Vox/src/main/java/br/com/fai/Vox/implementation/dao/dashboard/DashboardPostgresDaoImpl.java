package br.com.fai.Vox.implementation.dao.dashboard;

import br.com.fai.Vox.domain.dto.dashboard.CategoryAnalysisDto;
import br.com.fai.Vox.domain.dto.dashboard.DashboardOverviewDto;
import br.com.fai.Vox.domain.dto.dashboard.DateRangeFilter;
import br.com.fai.Vox.domain.dto.dashboard.EngagementDto;
import br.com.fai.Vox.domain.dto.dashboard.HotspotDetailDto;
import br.com.fai.Vox.domain.dto.dashboard.MapPointDto;
import br.com.fai.Vox.domain.dto.dashboard.ModerationHealthDto;
import br.com.fai.Vox.domain.dto.dashboard.NeighborhoodHotspotDto;
import br.com.fai.Vox.domain.dto.dashboard.ProjectLifecycleDto;
import br.com.fai.Vox.domain.dto.dashboard.TimeSeriesDto;
import br.com.fai.Vox.port.dao.dashboard.DashboardDao;

import java.math.BigDecimal;
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

    @Override
    public List<MapPointDto> getMapHotspots(int municipalityId, int precision, DateRangeFilter dateRange) {
        final String sql =
                "SELECT lat, lng, " +
                "       SUM(CASE WHEN src = 'issue' THEN 1 ELSE 0 END) AS issue_count, " +
                "       SUM(CASE WHEN src = 'project' THEN 1 ELSE 0 END) AS project_count " +
                "FROM ( " +
                "   SELECT ROUND(latitude, ?) AS lat, ROUND(longitude, ?) AS lng, 'issue' AS src " +
                "   FROM issue_report WHERE municipality_id = ? AND moderation_status = 'APPROVED' " +
                "     AND latitude IS NOT NULL AND longitude IS NOT NULL" +
                        dateClause("created_at", dateRange) + " " +
                "   UNION ALL " +
                "   SELECT ROUND(latitude, ?) AS lat, ROUND(longitude, ?) AS lng, 'project' AS src " +
                "   FROM project WHERE municipality_id = ? AND moderation_status = 'APPROVED' " +
                "     AND latitude IS NOT NULL AND longitude IS NOT NULL" +
                        dateClause("created_at", dateRange) + " " +
                ") grid " +
                "GROUP BY lat, lng " +
                "ORDER BY (SUM(CASE WHEN src = 'issue' THEN 1 ELSE 0 END) " +
                "        + SUM(CASE WHEN src = 'project' THEN 1 ELSE 0 END)) DESC";

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
                "ORDER BY (SUM(CASE WHEN src = 'issue' THEN 1 ELSE 0 END) " +
                "        + SUM(CASE WHEN src = 'project' THEN 1 ELSE 0 END)) DESC";

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

    @Override
    public EngagementDto getEngagement(int municipalityId, DateRangeFilter dateRange) {
        EngagementDto dto = new EngagementDto();

        dto.setTopSignedProjects(fetchRankedProjects(
                "SELECT p.id AS project_id, p.title AS title, COUNT(s.id) AS c " +
                "FROM project p JOIN project_signature s ON s.project_id = p.id " +
                "WHERE p.municipality_id = ?" + dateClause("s.created_at", dateRange) + " " +
                "GROUP BY p.id, p.title ORDER BY c DESC LIMIT 10",
                municipalityId, dateRange));

        dto.setTopOpinedProjects(fetchRankedProjects(
                "SELECT p.id AS project_id, p.title AS title, COUNT(o.id) AS c " +
                "FROM project p JOIN project_opinion o ON o.project_id = p.id " +
                "WHERE p.municipality_id = ?" + dateClause("o.created_at", dateRange) + " " +
                "GROUP BY p.id, p.title ORDER BY c DESC LIMIT 10",
                municipalityId, dateRange));

        dto.setOpinionDistribution(countGroupBy(
                "SELECT o.opinion::text AS k, COUNT(*) AS c " +
                "FROM project_opinion o JOIN project p ON p.id = o.project_id " +
                "WHERE p.municipality_id = ?" + dateClause("o.created_at", dateRange) + " " +
                "GROUP BY o.opinion",
                municipalityId, dateRange));

        dto.setTopFollowedIssues(fetchRankedIssues(
                "SELECT i.id AS issue_id, i.title AS title, COUNT(s.id) AS c " +
                "FROM issue_report i JOIN subscription s ON s.issue_id = i.id AND s.type = 'ISSUE' " +
                "WHERE i.municipality_id = ?" + dateClause("s.created_at", dateRange) + " " +
                "GROUP BY i.id, i.title ORDER BY c DESC LIMIT 10",
                municipalityId, dateRange));

        dto.setTopActiveUsers(fetchActiveUsers(municipalityId, dateRange));

        return dto;
    }

    private List<EngagementDto.RankedProjectDto> fetchRankedProjects(String sql, int municipalityId,
                                                                     DateRangeFilter dateRange) {
        final List<EngagementDto.RankedProjectDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new EngagementDto.RankedProjectDto(
                            rs.getInt("project_id"), rs.getString("title"), rs.getLong("c")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private List<EngagementDto.RankedIssueDto> fetchRankedIssues(String sql, int municipalityId,
                                                                 DateRangeFilter dateRange) {
        final List<EngagementDto.RankedIssueDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new EngagementDto.RankedIssueDto(
                            rs.getInt("issue_id"), rs.getString("title"), rs.getLong("c")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private List<EngagementDto.ActiveUserDto> fetchActiveUsers(int municipalityId, DateRangeFilter dateRange) {
        final String sql =
                "SELECT u.id AS user_id, u.name AS user_name, " +
                "       SUM(CASE WHEN src = 'project' THEN 1 ELSE 0 END) AS projects_created, " +
                "       SUM(CASE WHEN src = 'issue' THEN 1 ELSE 0 END) AS issues_created " +
                "FROM ( " +
                "   SELECT author_id, 'project' AS src FROM project " +
                "   WHERE municipality_id = ?" + dateClause("created_at", dateRange) + " " +
                "   UNION ALL " +
                "   SELECT author_id, 'issue' AS src FROM issue_report " +
                "   WHERE municipality_id = ?" + dateClause("created_at", dateRange) + " " +
                ") a JOIN user_model u ON u.id = a.author_id " +
                "GROUP BY u.id, u.name " +
                "ORDER BY (SUM(CASE WHEN src = 'project' THEN 1 ELSE 0 END) " +
                "        + SUM(CASE WHEN src = 'issue' THEN 1 ELSE 0 END)) DESC LIMIT 10";

        final List<EngagementDto.ActiveUserDto> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            idx = bindDateRange(ps, idx, dateRange);
            ps.setInt(idx++, municipalityId);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new EngagementDto.ActiveUserDto(
                            rs.getInt("user_id"), rs.getString("user_name"),
                            rs.getLong("projects_created"), rs.getLong("issues_created")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public CategoryAnalysisDto getCategoryAnalysis(int municipalityId, DateRangeFilter dateRange) {
        Map<Integer, long[]> counts = new LinkedHashMap<>();
        Map<Integer, String> names = new LinkedHashMap<>();

        final String issueSql =
                "SELECT c.id AS category_id, c.name AS category_name, COUNT(i.id) AS total, " +
                "       SUM(CASE WHEN i.status = 'OPEN' THEN 1 ELSE 0 END) AS open_count " +
                "FROM category c JOIN issue_report i ON i.category_id = c.id " +
                "WHERE i.municipality_id = ?" + dateClause("i.created_at", dateRange) + " " +
                "GROUP BY c.id, c.name";
        try (PreparedStatement ps = connection.prepareStatement(issueSql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int catId = rs.getInt("category_id");
                    names.put(catId, rs.getString("category_name"));
                    long[] arr = counts.computeIfAbsent(catId, k -> new long[3]);
                    arr[0] = rs.getLong("total");
                    arr[2] = rs.getLong("open_count");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        final String projectSql =
                "SELECT c.id AS category_id, c.name AS category_name, COUNT(p.id) AS total " +
                "FROM category c JOIN project p ON p.category_id = c.id " +
                "WHERE p.municipality_id = ?" + dateClause("p.created_at", dateRange) + " " +
                "GROUP BY c.id, c.name";
        try (PreparedStatement ps = connection.prepareStatement(projectSql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int catId = rs.getInt("category_id");
                    names.put(catId, rs.getString("category_name"));
                    long[] arr = counts.computeIfAbsent(catId, k -> new long[3]);
                    arr[1] = rs.getLong("total");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Map<Integer, Map<String, Long>> issuesByStatus = categoryStatusBreakdown(
                "SELECT i.category_id AS category_id, i.status::text AS st, COUNT(*) AS c " +
                "FROM issue_report i WHERE i.municipality_id = ?" + dateClause("i.created_at", dateRange) + " " +
                "GROUP BY i.category_id, i.status",
                municipalityId, dateRange);
        Map<Integer, Map<String, Long>> projectsByStatus = categoryStatusBreakdown(
                "SELECT p.category_id AS category_id, p.status::text AS st, COUNT(*) AS c " +
                "FROM project p WHERE p.municipality_id = ?" + dateClause("p.created_at", dateRange) + " " +
                "GROUP BY p.category_id, p.status",
                municipalityId, dateRange);

        List<CategoryAnalysisDto.CategoryStatDto> categories = new ArrayList<>();
        for (Map.Entry<Integer, long[]> e : counts.entrySet()) {
            int catId = e.getKey();
            long[] arr = e.getValue();
            CategoryAnalysisDto.CategoryStatDto stat = new CategoryAnalysisDto.CategoryStatDto(
                    catId, names.get(catId), arr[0], arr[1]);
            stat.setOpenIssues(arr[2]);
            stat.setIssuesByStatus(issuesByStatus.getOrDefault(catId, new LinkedHashMap<>()));
            stat.setProjectsByStatus(projectsByStatus.getOrDefault(catId, new LinkedHashMap<>()));
            categories.add(stat);
        }
        categories.sort((a, b) -> Long.compare(b.getTotal(), a.getTotal()));

        return new CategoryAnalysisDto(categories);
    }

    private Map<Integer, Map<String, Long>> categoryStatusBreakdown(String sql, int municipalityId,
                                                                    DateRangeFilter dateRange) {
        Map<Integer, Map<String, Long>> result = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int catId = rs.getInt("category_id");
                    result.computeIfAbsent(catId, k -> new LinkedHashMap<>())
                            .put(rs.getString("st"), rs.getLong("c"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public TimeSeriesDto getTimeSeries(int municipalityId, String granularity, DateRangeFilter dateRange) {
        final String sql =
                "SELECT to_char(period, 'YYYY-MM-DD') AS period, " +
                "       SUM(CASE WHEN src = 'issue' THEN 1 ELSE 0 END) AS issue_count, " +
                "       SUM(CASE WHEN src = 'project' THEN 1 ELSE 0 END) AS project_count " +
                "FROM ( " +
                "   SELECT date_trunc('" + granularity + "', created_at) AS period, 'issue' AS src " +
                "   FROM issue_report WHERE municipality_id = ?" + dateClause("created_at", dateRange) + " " +
                "   UNION ALL " +
                "   SELECT date_trunc('" + granularity + "', created_at) AS period, 'project' AS src " +
                "   FROM project WHERE municipality_id = ?" + dateClause("created_at", dateRange) + " " +
                ") t GROUP BY period ORDER BY period ASC";

        final List<TimeSeriesDto.TimeSeriesPointDto> points = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            idx = bindDateRange(ps, idx, dateRange);
            ps.setInt(idx++, municipalityId);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    points.add(new TimeSeriesDto.TimeSeriesPointDto(
                            rs.getString("period"),
                            rs.getLong("issue_count"),
                            rs.getLong("project_count")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new TimeSeriesDto(granularity, points);
    }

    @Override
    public ProjectLifecycleDto getProjectLifecycle(int municipalityId, DateRangeFilter dateRange) {
        ProjectLifecycleDto dto = new ProjectLifecycleDto();

        dto.setProjectsByStatus(countGroupBy(
                "SELECT status::text AS k, COUNT(*) AS c FROM project WHERE municipality_id = ?"
                        + dateClause("created_at", dateRange) + " GROUP BY status",
                municipalityId, dateRange));

        final String stageSql =
                "SELECT status, AVG(hours) AS avg_hours FROM ( " +
                "   SELECT h.previous_status::text AS status, " +
                "          EXTRACT(EPOCH FROM (" +
                "              LEAD(h.created_at) OVER (PARTITION BY h.project_id ORDER BY h.created_at) - h.created_at" +
                "          )) / 3600.0 AS hours " +
                "   FROM project_status_history h JOIN project p ON p.id = h.project_id " +
                "   WHERE p.municipality_id = ?" + dateClause("h.created_at", dateRange) + " " +
                ") s WHERE hours IS NOT NULL AND status IS NOT NULL " +
                "GROUP BY status ORDER BY avg_hours DESC";

        List<ProjectLifecycleDto.StageDurationDto> stages = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(stageSql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stages.add(new ProjectLifecycleDto.StageDurationDto(
                            rs.getString("status"), round1(rs.getDouble("avg_hours"))));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        dto.setAvgTimePerStage(stages);

        final String budgetSql =
                "SELECT COALESCE(SUM(estimated_cost), 0) AS est, COALESCE(SUM(approved_budget), 0) AS appr " +
                "FROM project WHERE municipality_id = ?" + dateClause("created_at", dateRange);
        try (PreparedStatement ps = connection.prepareStatement(budgetSql)) {
            int idx = 1;
            ps.setInt(idx++, municipalityId);
            bindDateRange(ps, idx, dateRange);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal est = rs.getBigDecimal("est");
                    BigDecimal appr = rs.getBigDecimal("appr");
                    dto.setTotalEstimatedCost(est);
                    dto.setTotalApprovedBudget(appr);
                    if (est != null && est.signum() > 0) {
                        dto.setBudgetExecutionRate(round1(
                                appr.doubleValue() * 100.0 / est.doubleValue()));
                    } else {
                        dto.setBudgetExecutionRate(null);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return dto;
    }

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
