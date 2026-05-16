package br.com.fai.Vox.implementation.service.project;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.ProjectImage;
import br.com.fai.Vox.domain.dto.CreateProjectDto;
import br.com.fai.Vox.port.dao.project.ProjectDao;
import br.com.fai.Vox.port.dao.projectimage.ProjectImageDao;
import br.com.fai.Vox.port.service.drive.GoogleDriveService;
import br.com.fai.Vox.port.service.project.ProjectService;
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
    private final GoogleDriveService googleDriveService;

    public ProjectServiceImpl(ProjectDao projectDao, ProjectImageDao projectImageDao, GoogleDriveService googleDriveService) {
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

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            try {
                String fileName = "project_" + projectId + "_" + dto.getFile().getOriginalFilename();
                String url = googleDriveService.uploadFile(dto.getFile(), fileName);

                ProjectImage image = new ProjectImage();
                image.setProjectId(projectId);
                image.setUrl(url);
                projectImageDao.create(image);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Erro ao fazer upload da imagem para o Google Drive. ID do projeto: " + projectId, e);
            }
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
    public void update(int id, Project entity) {
        if (id != entity.getId()) return;
        if (findByid(id) == null) return;
        projectDao.update(id, entity);
    }
}
