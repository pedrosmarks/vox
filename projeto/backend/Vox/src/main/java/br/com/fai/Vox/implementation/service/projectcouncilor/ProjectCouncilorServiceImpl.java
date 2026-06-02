package br.com.fai.Vox.implementation.service.projectcouncilor;

import br.com.fai.Vox.domain.ProjectCouncilor;
import br.com.fai.Vox.port.dao.projectcouncilor.ProjectCouncilorDao;
import br.com.fai.Vox.port.service.projectcouncilor.ProjectCouncilorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectCouncilorServiceImpl implements ProjectCouncilorService {

    private final ProjectCouncilorDao projectCouncilorDao;

    public ProjectCouncilorServiceImpl(ProjectCouncilorDao projectCouncilorDao) {
        this.projectCouncilorDao = projectCouncilorDao;
    }

    @Override
    public void add(int projectId, int councilorId) {
        if (projectId <= 0 || councilorId <= 0) return;
        projectCouncilorDao.add(projectId, councilorId);
    }

    @Override
    public void remove(int projectId, int councilorId) {
        if (projectId <= 0 || councilorId <= 0) return;
        projectCouncilorDao.remove(projectId, councilorId);
    }

    @Override
    public List<ProjectCouncilor> findByProjectId(int projectId) {
        if (projectId <= 0) return List.of();
        return projectCouncilorDao.findByProjectId(projectId);
    }
}
