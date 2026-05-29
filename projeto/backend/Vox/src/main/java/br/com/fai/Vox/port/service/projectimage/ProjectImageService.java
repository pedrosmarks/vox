package br.com.fai.Vox.port.service.projectimage;

import br.com.fai.Vox.domain.ProjectImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProjectImageService {
    int create(int projectId, MultipartFile file);
    void update(int id, MultipartFile file);
    void delete(int id);
    ProjectImage findByid(int id);
    List<ProjectImage> findAll();
    List<ProjectImage> findByProjectId(int projectId);
}
