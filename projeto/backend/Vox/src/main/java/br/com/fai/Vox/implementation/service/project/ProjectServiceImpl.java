package br.com.fai.Vox.implementation.service.project;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.port.dao.project.ProjectDao;
import br.com.fai.Vox.port.service.projetc.ProjectService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectDao projectDao;

    public ProjectServiceImpl(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @Override
    public int create(Project entity) {
        int invalidResponse = -1;

        if (entity == null) {
            return invalidResponse;
        }

        if (entity.getTitle().isEmpty() || entity.getDescription().isEmpty()) {
            return invalidResponse;
        }

        final int id = projectDao.create(entity);
        return id;
    }

    @Override
    public void delete(int id) {
        if (id < 0) {
            return;
        }

        projectDao.delete(id);
    }

    @Override
    public Project findByid(int id) {
        if (id < 0) {
            return null;
        }

        Project entity = projectDao.findByid(id);
        return entity;
    }

    @Override
    public List<Project> findAll() {
        final List<Project> entities = projectDao.findAll();
        return entities;
    }

    @Override
    public void update(int id, Project entity) {
        if (id != entity.getId()) {
            return;
        }

        Project project = findByid(id);
        if (project == null) {
            return;
        }

        projectDao.update(id, entity);
    }
}
