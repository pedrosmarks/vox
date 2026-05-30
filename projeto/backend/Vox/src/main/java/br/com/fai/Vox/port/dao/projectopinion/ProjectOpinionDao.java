package br.com.fai.Vox.port.dao.projectopinion;

import br.com.fai.Vox.domain.ProjectOpinion;

import java.util.List;

public interface ProjectOpinionDao {
    void upsert(ProjectOpinion entity);
    List<ProjectOpinion> findByProjectId(int projectId);
    ProjectOpinion findByProjectIdAndUserId(int projectId, int userId);
}
