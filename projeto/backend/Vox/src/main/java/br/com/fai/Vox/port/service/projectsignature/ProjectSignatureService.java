package br.com.fai.Vox.port.service.projectsignature;

import br.com.fai.Vox.domain.ProjectSignature;

import java.util.List;

public interface ProjectSignatureService {
    void sign(int projectId, int userId);
    void unsign(int projectId, int userId);
    boolean hasSignature(int projectId, int userId);
    long countByProjectId(int projectId);
    List<ProjectSignature> findByProjectId(int projectId);
}
