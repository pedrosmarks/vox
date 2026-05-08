package br.com.fai.Vox.implementation.service.projectimage;

import br.com.fai.Vox.domain.ProjectImage;
import br.com.fai.Vox.port.dao.projectimage.ProjectImageDao;
import br.com.fai.Vox.port.service.projectimage.ProjectImageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectImageServiceImpl implements ProjectImageService {

    private final ProjectImageDao projectImageDao;

    public ProjectImageServiceImpl(ProjectImageDao projectImageDao) {
        this.projectImageDao = projectImageDao;
    }

    @Override
    public int create(ProjectImage entity) {
        int invalidResponse = -1;

        if (entity == null) {
            return invalidResponse;
        }

        if (entity.getUrl().isEmpty() || entity.getProjectId() > 0) {
            return invalidResponse;
        }

        final int id = projectImageDao.create(entity);
        return id;
    }

    @Override
    public void delete(int id) {
        if (id < 0) {
            return;
        }

        projectImageDao.delete(id);
    }

    @Override
    public ProjectImage findByid(int id) {
        if (id < 0) {
            return null;
        }

        ProjectImage entity = projectImageDao.findByid(id);
        return entity;
    }

    @Override
    public List<ProjectImage> findAll() {
        final List<ProjectImage> entities = projectImageDao.findAll();
        return entities;
    }

    @Override
    public void update(int id, ProjectImage entity) {
        if (id != entity.getId()) {
            return;
        }

        ProjectImage projectImage = findByid(id);
        if (projectImage == null) {
            return;
        }

        projectImageDao.update(id, entity);
    }

    @Override
    public List<ProjectImage> findByProjectId(int projectId) {
        if (projectId <= 0) {
            return new ArrayList<>();
        }

        List<ProjectImage> projectImages = projectImageDao.findByProjectId(projectId);

        return projectImages;
    }
}
