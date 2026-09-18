package br.com.fai.Vox.implementation.service.projectsignature;

import br.com.fai.Vox.domain.ProjectSignature;
import br.com.fai.Vox.port.dao.projectsignature.ProjectSignatureDao;
import br.com.fai.Vox.port.service.projectsignature.ProjectSignatureService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectSignatureServiceImpl implements ProjectSignatureService {

    private final ProjectSignatureDao projectSignatureDao;

    public ProjectSignatureServiceImpl(ProjectSignatureDao projectSignatureDao) {
        this.projectSignatureDao = projectSignatureDao;
    }

    @Override
    public void sign(int projectId, int userId) {
        if (projectId <= 0 || userId <= 0) return;
        projectSignatureDao.sign(projectId, userId);
    }

    @Override
    public void unsign(int projectId, int userId) {
        if (projectId <= 0 || userId <= 0) return;
        projectSignatureDao.unsign(projectId, userId);
    }

    @Override
    public boolean hasSignature(int projectId, int userId) {
        if (projectId <= 0 || userId <= 0) return false;
        return projectSignatureDao.exists(projectId, userId);
    }

    @Override
    public long countByProjectId(int projectId) {
        if (projectId <= 0) return 0;
        return projectSignatureDao.countByProjectId(projectId);
    }

    @Override
    public List<ProjectSignature> findByProjectId(int projectId) {
        if (projectId <= 0) return List.of();
        return projectSignatureDao.findByProjectId(projectId);
    }
}
