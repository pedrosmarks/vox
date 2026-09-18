package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Ciclo de vida dos projetos: distribuição por status, tempo médio em cada
 * etapa (via project_status_history) e execução orçamentária
 * (estimated_cost vs approved_budget). Escopado por município e intervalo de datas.
 */
@Getter
@Setter
public class ProjectLifecycleDto {

    /** Quantidade de projetos em cada status (project_status -> total). */
    private Map<String, Long> projectsByStatus;

    /** Tempo médio (em horas) que os projetos permaneceram em cada status. */
    private List<StageDurationDto> avgTimePerStage;

    // --- Execução orçamentária ---
    private BigDecimal totalEstimatedCost;
    private BigDecimal totalApprovedBudget;
    /** Percentual do orçamento aprovado sobre o custo estimado (0..100+). */
    private Double budgetExecutionRate;

    public ProjectLifecycleDto() {}

    @Getter
    @Setter
    public static class StageDurationDto {
        /** Status/etapa de origem da transição. */
        private String status;
        /** Tempo médio, em horas, permanecido nessa etapa antes de mudar. */
        private double avgHours;

        public StageDurationDto() {}

        public StageDurationDto(String status, double avgHours) {
            this.status = status;
            this.avgHours = avgHours;
        }
    }
}
