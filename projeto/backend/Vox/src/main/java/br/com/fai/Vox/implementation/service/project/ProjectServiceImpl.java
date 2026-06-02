package br.com.fai.Vox.implementation.service.project;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.ProjectImage;
import br.com.fai.Vox.domain.dto.CreateProjectDto;
import br.com.fai.Vox.port.dao.project.ProjectDao;
import br.com.fai.Vox.port.dao.projectimage.ProjectImageDao;
import br.com.fai.Vox.port.service.drive.CloudinaryService;
import br.com.fai.Vox.port.service.project.ProjectService;
import br.com.fai.Vox.port.service.projectstatushistory.ProjectStatusHistoryService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = Logger.getLogger(ProjectServiceImpl.class.getName());

    private final ProjectDao projectDao;
    private final ProjectImageDao projectImageDao;
    private final CloudinaryService cloudinaryService;
    private final ProjectStatusHistoryService projectStatusHistoryService;

    public ProjectServiceImpl(ProjectDao projectDao, ProjectImageDao projectImageDao,
                               CloudinaryService cloudinaryService,
                               ProjectStatusHistoryService projectStatusHistoryService) {
        this.projectDao = projectDao;
        this.projectImageDao = projectImageDao;
        this.cloudinaryService = cloudinaryService;
        this.projectStatusHistoryService = projectStatusHistoryService;
    }

    @Override
    public int create(CreateProjectDto dto) {
        if (dto == null || dto.getTitle() == null || dto.getTitle().isEmpty()) return -1;

        final int projectId = projectDao.create(dto);
        logger.log(Level.INFO, "Projeto criado. ID: " + projectId);

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            try {
                String fileName = "project_" + projectId + "_" + dto.getFile().getOriginalFilename();
                String url = cloudinaryService.uploadFile(dto.getFile(), fileName);

                ProjectImage image = new ProjectImage();
                image.setProjectId(projectId);
                image.setUrl(url);
                projectImageDao.create(image);
                logger.log(Level.INFO, "Imagem salva no banco para o projeto ID: " + projectId);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Erro ao processar imagem para o projeto ID: " + projectId, e);
            }
        }

        return projectId;
    }

    @Override
    public void delete(int id) {
        if (id <= 0) return;
        projectDao.delete(id);
    }

    @Override
    public Project findByid(int id) {
        if (id <= 0) return null;
        return projectDao.findByid(id);
    }

    @Override
    public List<Project> findAll() {
        return projectDao.findAll();
    }

    @Override
    public List<Project> findByMunicipalityId(int municipalityId) {
        if (municipalityId <= 0) return List.of();
        return projectDao.findByMunicipalityId(municipalityId);
    }

    @Override
    public List<Project> findByAuthorId(int authorId) {
        if (authorId <= 0) return List.of();
        return projectDao.findByAuthorId(authorId);
    }

    @Override
    public void update(int id, Project entity, int changedBy) {
        if (id <= 0) return;

        Project existing = findByid(id);
        if (existing == null) return;

        if (existing.getStatus() != entity.getStatus()) {
            projectStatusHistoryService.recordStatusChange(
                    id, existing.getStatus(), entity.getStatus(), changedBy, null);
        }

        projectDao.update(id, entity);
    }
}
