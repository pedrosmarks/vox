package br.com.fai.Vox.port.dao.projectmoderation;

import br.com.fai.Vox.domain.ProjectModeration;

import java.util.List;

public interface ProjectModerationDao {
    void create(ProjectModeration entity);
    List<ProjectModeration> findByProjectId(int projectId);
}
