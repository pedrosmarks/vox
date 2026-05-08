package br.com.fai.Vox.port.service.projectimage;

import br.com.fai.Vox.domain.ProjectImage;
import br.com.fai.Vox.port.service.crud.CrudService;

public interface ProjectImageService extends CrudService<ProjectImage>, ReadByProjectIdService {
}
