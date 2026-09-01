package br.com.fai.Vox.port.dao.projectsignature;

import br.com.fai.Vox.domain.ProjectSignature;

import java.util.List;

public interface ProjectSignatureDao {
    void sign(int projectId, int userId);
    void unsign(int projectId, int userId);
    boolean exists(int projectId, int userId);
    long countByProjectId(int projectId);
    List<ProjectSignature> findByProjectId(int projectId);
    ProjectSignature findByProjectIdAndUserId(int projectId, int userId);
}
