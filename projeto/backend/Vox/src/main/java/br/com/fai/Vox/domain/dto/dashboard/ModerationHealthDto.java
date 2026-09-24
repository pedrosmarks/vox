package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ModerationHealthDto {

    private long pendingIssues;
    private long pendingProjects;

    private long approvedIssues;
    private long rejectedIssues;
    private long approvedProjects;
    private long rejectedProjects;

    private double issueApprovalRate;
    private double projectApprovalRate;

    private Double avgIssueDecisionHours;
    private Double avgProjectDecisionHours;

    private List<ModeratorStatDto> topModerators;

    public ModerationHealthDto() {}

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
