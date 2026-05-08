package br.com.fai.Vox.port.service.projectimage;

import br.com.fai.Vox.domain.ProjectImage;

import java.util.List;

public interface ReadByProjectIdService {
    List<ProjectImage> findByProjectId(final int projectId);
}
