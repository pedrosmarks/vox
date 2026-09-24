package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class EngagementDto {

    private List<RankedProjectDto> topSignedProjects;

    private List<RankedProjectDto> topOpinedProjects;

    private Map<String, Long> opinionDistribution;

    private List<RankedIssueDto> topFollowedIssues;

    private List<ActiveUserDto> topActiveUsers;

    public EngagementDto() {}

    @Getter
    @Setter
    public static class RankedProjectDto {
        private int projectId;
        private String title;
        private long count;

        public RankedProjectDto() {}

        public RankedProjectDto(int projectId, String title, long count) {
            this.projectId = projectId;
            this.title = title;
            this.count = count;
        }
    }

    @Getter
    @Setter
    public static class RankedIssueDto {
        private int issueId;
        private String title;
        private long count;

        public RankedIssueDto() {}

        public RankedIssueDto(int issueId, String title, long count) {
            this.issueId = issueId;
            this.title = title;
            this.count = count;
        }
    }

    @Getter
    @Setter
    public static class ActiveUserDto {
        private int userId;
        private String userName;
        private long projectsCreated;
        private long issuesCreated;
        private long total;

        public ActiveUserDto() {}

        public ActiveUserDto(int userId, String userName, long projectsCreated, long issuesCreated) {
            this.userId = userId;
            this.userName = userName;
            this.projectsCreated = projectsCreated;
            this.issuesCreated = issuesCreated;
            this.total = projectsCreated + issuesCreated;
        }
    }
}
