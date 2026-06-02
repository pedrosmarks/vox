package br.com.fai.Vox.port.dao.projectstatushistory;

import br.com.fai.Vox.domain.ProjectStatusHistory;

import java.util.List;

public interface ProjectStatusHistoryDao {
    void create(ProjectStatusHistory entity);
    List<ProjectStatusHistory> findByProjectId(int projectId);
}
