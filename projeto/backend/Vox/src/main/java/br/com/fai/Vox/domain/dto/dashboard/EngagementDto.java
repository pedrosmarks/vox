package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Engajamento cidadão: mostra o que a população mais apoia, opina e acompanha.
 * Escopado por município e pelo intervalo de datas (sobre {@code created_at}).
 */
@Getter
@Setter
public class EngagementDto {

    /** Projetos com mais assinaturas de apoio (project_signature). */
    private List<RankedProjectDto> topSignedProjects;

    /** Projetos com mais opiniões registradas (project_opinion). */
    private List<RankedProjectDto> topOpinedProjects;

    /** Distribuição global de opiniões por tipo (APPROVE, NEUTRAL). */
    private Map<String, Long> opinionDistribution;

    /** Ocorrências mais acompanhadas (subscription do tipo ISSUE). */
    private List<RankedIssueDto> topFollowedIssues;

    /** Usuários mais ativos (por volume de projetos + issues criados). */
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
