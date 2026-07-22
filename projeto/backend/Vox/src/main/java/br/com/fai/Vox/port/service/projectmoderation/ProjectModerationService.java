package br.com.fai.Vox.port.service.projectmoderation;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.ProjectModeration;
import br.com.fai.Vox.domain.dto.PageResponse;

import java.util.List;

public interface ProjectModerationService {
    List<Project> findPending(int municipalityId);
    PageResponse<Project> findPending(int municipalityId, int page, int size);
    void approve(int projectId, int moderatorId, String feedback);
    void reject(int projectId, int moderatorId, String feedback);
    void updateStatus(int projectId, Project.ProjectStatus status, int moderatorId, String note);
    List<ProjectModeration> findByProjectId(int projectId);
}
