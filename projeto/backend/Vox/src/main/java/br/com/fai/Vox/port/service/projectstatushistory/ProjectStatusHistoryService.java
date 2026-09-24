package br.com.fai.Vox.port.service.projectstatushistory;
import br.com.fai.Vox.domain.enums.ProjectStatusEnum;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.ProjectStatusHistory;

import java.util.List;

public interface ProjectStatusHistoryService {
    void recordStatusChange(int projectId, ProjectStatusEnum previousStatus, ProjectStatusEnum newStatus, int changedBy, String note);
    List<ProjectStatusHistory> findByProjectId(int projectId);
}
