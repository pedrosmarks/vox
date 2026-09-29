package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class DashboardOverviewDto {

    private long totalIssues;
    private long approvedIssues;
    private long pendingModerationIssues;
    private long resolvedIssues;
    private Map<String, Long> issuesByStatus;
    private Map<String, Long> issuesByModeration;

    private long totalProjects;
    private long publishedProjects;
    private long pendingModerationProjects;
    private Map<String, Long> projectsByStatus;
    private Map<String, Long> projectsByModeration;

    private long totalUsers;
    private Map<String, Long> usersByRole;

    private double issueResolutionRate;

    public DashboardOverviewDto() {}
}
