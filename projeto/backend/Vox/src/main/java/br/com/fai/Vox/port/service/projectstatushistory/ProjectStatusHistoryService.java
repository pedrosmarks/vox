package br.com.fai.Vox.port.service.projectstatushistory;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.ProjectStatusHistory;

import java.util.List;

public interface ProjectStatusHistoryService {
    void recordStatusChange(int projectId, Project.ProjectStatus previousStatus, Project.ProjectStatus newStatus, int changedBy, String note);
    List<ProjectStatusHistory> findByProjectId(int projectId);
}
