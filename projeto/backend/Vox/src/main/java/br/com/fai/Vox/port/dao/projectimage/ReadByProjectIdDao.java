package br.com.fai.Vox.port.dao.projectimage;

import br.com.fai.Vox.domain.ProjectImage;

import java.util.List;

public interface ReadByProjectIdDao {
    List<ProjectImage> findByProjectId(final int projectId);
}
