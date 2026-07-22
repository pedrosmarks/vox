package br.com.fai.Vox.port.dao.projectopinion;

import br.com.fai.Vox.domain.ProjectOpinion;

import java.util.List;
import java.util.Map;

public interface ProjectOpinionDao {
    void upsert(ProjectOpinion entity);
    List<ProjectOpinion> findByProjectId(int projectId);
    ProjectOpinion findByProjectIdAndUserId(int projectId, int userId);
    Map<String, Integer> countByProjectId(int projectId);
}
