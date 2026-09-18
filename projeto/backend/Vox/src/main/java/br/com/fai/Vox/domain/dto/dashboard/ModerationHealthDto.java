package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Saúde da fila de moderação, escopada por município: backlog pendente,
 * taxas de aprovação/rejeição, tempo médio de decisão e ranking de moderadores.
 */
@Getter
@Setter
public class ModerationHealthDto {

    // Backlog atual (moderation_status = PENDING)
    private long pendingIssues;
    private long pendingProjects;

    // Volume de decisões já tomadas (tabelas *_moderation)
    private long approvedIssues;
    private long rejectedIssues;
    private long approvedProjects;
    private long rejectedProjects;

    // Taxas de aprovação (0..100) sobre o total de decisões
    private double issueApprovalRate;
    private double projectApprovalRate;

    // Tempo médio, em horas, entre a criação do registro e a decisão de moderação
    private Double avgIssueDecisionHours;
    private Double avgProjectDecisionHours;

    // Ranking de moderadores por volume de decisões
    private List<ModeratorStatDto> topModerators;

    public ModerationHealthDto() {}

    /**
     * Estatística por moderador: quantas decisões tomou no município.
     */
    @Getter
    @Setter
    public static class ModeratorStatDto {
        private int moderatorId;
        private String moderatorName;
        private long decisions;

        public ModeratorStatDto() {}

        public ModeratorStatDto(int moderatorId, String moderatorName, long decisions) {
            this.moderatorId = moderatorId;
            this.moderatorName = moderatorName;
            this.decisions = decisions;
        }
    }
}
