package br.com.fai.Vox.port.service.projectopinion;

import br.com.fai.Vox.domain.ProjectOpinion;

import java.util.List;

public interface ProjectOpinionService {
    void submitOpinion(int projectId, int userId, ProjectOpinion.OpinionType opinion);
    List<ProjectOpinion> findByProjectId(int projectId);
    ProjectOpinion findByProjectIdAndUserId(int projectId, int userId);
}
