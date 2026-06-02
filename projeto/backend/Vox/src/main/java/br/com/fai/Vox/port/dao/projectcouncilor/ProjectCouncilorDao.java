package br.com.fai.Vox.port.dao.projectcouncilor;

import br.com.fai.Vox.domain.ProjectCouncilor;

import java.util.List;

public interface ProjectCouncilorDao {
    void add(int projectId, int councilorId);
    void remove(int projectId, int councilorId);
    List<ProjectCouncilor> findByProjectId(int projectId);
}
