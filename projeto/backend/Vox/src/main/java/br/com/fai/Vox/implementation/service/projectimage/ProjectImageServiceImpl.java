package br.com.fai.Vox.implementation.service.projectimage;

import br.com.fai.Vox.domain.ProjectImage;
import br.com.fai.Vox.port.dao.projectimage.ProjectImageDao;
import br.com.fai.Vox.port.service.drive.CloudinaryService;
import br.com.fai.Vox.port.service.projectimage.ProjectImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ProjectImageServiceImpl implements ProjectImageService {

    private static final Logger logger = Logger.getLogger(ProjectImageServiceImpl.class.getName());

    private final ProjectImageDao projectImageDao;
    private final CloudinaryService cloudinaryService;

    public ProjectImageServiceImpl(ProjectImageDao projectImageDao, CloudinaryService cloudinaryService) {
        this.projectImageDao = projectImageDao;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public int create(int projectId, MultipartFile file) {
        if (projectId <= 0 || file == null || file.isEmpty()) return -1;

        try {
            String fileName = "project_" + projectId + "_" + file.getOriginalFilename();
            String url = cloudinaryService.uploadFile(file, fileName);

            ProjectImage image = new ProjectImage();
            image.setProjectId(projectId);
            image.setUrl(url);
            return projectImageDao.create(image);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao fazer upload da imagem para o projeto ID: " + projectId, e);
            return -1;
        }
    }

    @Override
    public void update(int id, MultipartFile file) {
        if (id <= 0 || file == null || file.isEmpty()) return;

        ProjectImage existing = projectImageDao.findByid(id);
        if (existing == null) return;

        try {
            String fileName = "project_image_" + id + "_" + file.getOriginalFilename();
            String url = cloudinaryService.uploadFile(file, fileName);
            existing.setUrl(url);
            projectImageDao.update(id, existing);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar imagem ID: " + id, e);
        }
    }

    @Override
    public void delete(int id) {
        if (id <= 0) return;
        projectImageDao.delete(id);
    }

    @Override
    public ProjectImage findByid(int id) {
        if (id <= 0) return null;
        return projectImageDao.findByid(id);
    }

    @Override
    public List<ProjectImage> findAll() {
        return projectImageDao.findAll();
    }

    @Override
    public List<ProjectImage> findByProjectId(int projectId) {
        if (projectId <= 0) return List.of();
        return projectImageDao.findByProjectId(projectId);
    }
}
