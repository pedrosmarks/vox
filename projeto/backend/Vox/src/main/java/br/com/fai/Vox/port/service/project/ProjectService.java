package br.com.fai.Vox.port.service.project;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.dto.CreateProjectDto;
import br.com.fai.Vox.port.service.crud.*;

import java.util.List;

public interface ProjectService extends CreateService<CreateProjectDto>, ReadService<Project>, UpdateService<Project>, DeleteService, ReadByMunicipalityIdService {
}
