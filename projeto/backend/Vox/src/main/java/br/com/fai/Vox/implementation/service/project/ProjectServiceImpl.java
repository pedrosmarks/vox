package br.com.fai.Vox.implementation.service.project;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.ProjectImage;
import br.com.fai.Vox.domain.dto.CreateProjectDto;
import br.com.fai.Vox.port.dao.project.ProjectDao;
import br.com.fai.Vox.port.dao.projectimage.ProjectImageDao;
import br.com.fai.Vox.port.service.drive.CloudinaryService;
import br.com.fai.Vox.port.service.project.ProjectService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Override
    public List<Project> findByStatus(Project.ProjectStatus status) {
        return projectDao.findByStatus(status);
    }

    @Override
    public List<Project> findByMunicipalityIdAndStatus(int municipalityId, Project.ProjectStatus status) {
        return projectDao.findByMunicipalityIdAndStatus(municipalityId, status);
    }

    @Override
    public void updateStatus(int id, Project.ProjectStatus status) {
        projectDao.updateStatus(id, status);
    }

    private static final Logger logger = Logger.getLogger(ProjectServiceImpl.class.getName());

    private final ProjectDao projectDao;
    private final ProjectImageDao projectImageDao;
    private final CloudinaryService googleDriveService;

    public ProjectServiceImpl(ProjectDao projectDao, ProjectImageDao projectImageDao, CloudinaryService googleDriveService) {
        this.projectDao = projectDao;
        this.projectImageDao = projectImageDao;
        this.googleDriveService = googleDriveService;
    }

    @Override
    public int create(CreateProjectDto dto) {
        if (dto == null || dto.getTitle() == null || dto.getTitle().isEmpty()) {
            return -1;
        }

        final int projectId = projectDao.create(dto);
        logger.log(Level.INFO, "Projeto criado. ID: " + projectId + " | File: " + (dto.getFile() != null ? dto.getFile().getOriginalFilename() + " size=" + dto.getFile().getSize() : "NULL"));

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            try {
                String fileName = "project_" + projectId + "_" + dto.getFile().getOriginalFilename();
                logger.log(Level.INFO, "Iniciando upload para o Google Drive. Arquivo: " + fileName);
                String url = googleDriveService.uploadFile(dto.getFile(), fileName);
                logger.log(Level.INFO, "Upload concluído. URL: " + url);

                ProjectImage image = new ProjectImage();
                image.setProjectId(projectId);
                image.setUrl(url);
                projectImageDao.create(image);
                logger.log(Level.INFO, "Imagem salva no banco para o projeto ID: " + projectId);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erro ao processar imagem para o projeto ID: " + projectId, e);
            }
        } else {
            logger.log(Level.WARNING, "Nenhum arquivo recebido no DTO para o projeto ID: " + projectId);
        }

        return projectId;
    }

    @Override
    public void delete(int id) {
        if (id < 0) return;
        projectDao.delete(id);
    }

    @Override
    public Project findByid(int id) {
        if (id < 0) return null;
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
    public void update(int id, Project entity) {
        if (id != entity.getId()) return;
        if (findByid(id) == null) return;
        projectDao.update(id, entity);
    }
}
