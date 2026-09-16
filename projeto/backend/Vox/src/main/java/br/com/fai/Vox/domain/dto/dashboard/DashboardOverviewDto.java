package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * KPIs de visão geral do painel administrativo, escopados por município.
 * Os mapas usam a chave = nome do status (ou tipo) e valor = contagem,
 * o que mantém a resposta flexível conforme os enums evoluem.
 */
@Getter
@Setter
public class DashboardOverviewDto {

    // --- Ocorrências (issues) ---
    private long totalIssues;
    private long approvedIssues;      // moderation_status = APPROVED
    private long pendingModerationIssues; // moderation_status = PENDING
    private long resolvedIssues;      // status = RESOLVED
    private Map<String, Long> issuesByStatus;         // issue_status -> total
    private Map<String, Long> issuesByModeration;     // moderation_status -> total

    // --- Projetos ---
    private long totalProjects;
    private long publishedProjects;   // status = PUBLISHED
    private long pendingModerationProjects;
    private Map<String, Long> projectsByStatus;       // project_status -> total
    private Map<String, Long> projectsByModeration;   // moderation_status -> total

    // --- Pessoas / engajamento ---
    private long totalUsers;
    private Map<String, Long> usersByRole;            // user_role -> total

    // --- Métricas derivadas ---
    /** Percentual de issues aprovadas que estão resolvidas (0..100). */
    private double issueResolutionRate;

    public DashboardOverviewDto() {}
}
