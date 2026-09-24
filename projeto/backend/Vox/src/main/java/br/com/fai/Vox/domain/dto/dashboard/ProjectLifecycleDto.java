package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProjectLifecycleDto {

    private Map<String, Long> projectsByStatus;

    private List<StageDurationDto> avgTimePerStage;

    private BigDecimal totalEstimatedCost;
    private BigDecimal totalApprovedBudget;
    private Double budgetExecutionRate;

    public ProjectLifecycleDto() {}

    @Getter
    @Setter
    public static class StageDurationDto {
        private String status;
        private double avgHours;

        public StageDurationDto() {}

        public StageDurationDto(String status, double avgHours) {
            this.status = status;
            this.avgHours = avgHours;
        }
    }
}
