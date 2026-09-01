package br.com.fai.Vox.port.service.projectopinion;

import br.com.fai.Vox.domain.ProjectOpinion;
import br.com.fai.Vox.domain.dto.ProjectOpinionStatsDto;

import java.util.List;
import java.util.Map;

public interface ProjectOpinionService {
    void submitOpinion(int projectId, int userId, ProjectOpinion.OpinionType opinion);
    List<ProjectOpinion> findByProjectId(int projectId);
    ProjectOpinion findByProjectIdAndUserId(int projectId, int userId);
    Map<String, Integer> countByProjectId(int projectId);
    ProjectOpinionStatsDto getStats(int projectId);
}
