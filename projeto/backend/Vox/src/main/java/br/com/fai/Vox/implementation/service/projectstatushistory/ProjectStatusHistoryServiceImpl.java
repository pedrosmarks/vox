package br.com.fai.Vox.implementation.service.projectstatushistory;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.ProjectStatusHistory;
import br.com.fai.Vox.port.dao.projectstatushistory.ProjectStatusHistoryDao;
import br.com.fai.Vox.port.service.projectstatushistory.ProjectStatusHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectStatusHistoryServiceImpl implements ProjectStatusHistoryService {

    private final ProjectStatusHistoryDao projectStatusHistoryDao;

    public ProjectStatusHistoryServiceImpl(ProjectStatusHistoryDao projectStatusHistoryDao) {
        this.projectStatusHistoryDao = projectStatusHistoryDao;
    }

    @Override
    public void recordStatusChange(int projectId, Project.ProjectStatus previousStatus, Project.ProjectStatus newStatus, int changedBy, String note) {
        ProjectStatusHistory history = new ProjectStatusHistory();
        history.setProjectId(projectId);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setNote(note);
        projectStatusHistoryDao.create(history);
    }

    @Override
    public List<ProjectStatusHistory> findByProjectId(int projectId) {
        if (projectId <= 0) return List.of();
        return projectStatusHistoryDao.findByProjectId(projectId);
    }
}
