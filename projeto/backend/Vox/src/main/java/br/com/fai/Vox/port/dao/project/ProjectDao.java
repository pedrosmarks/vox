package br.com.fai.Vox.port.dao.project;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.dto.CreateProjectDto;
import br.com.fai.Vox.port.dao.crud.CreateDao;
import br.com.fai.Vox.port.dao.crud.DeleteDao;
import br.com.fai.Vox.port.dao.crud.ReadDao;
import br.com.fai.Vox.port.dao.crud.UpdateDao;

public interface ProjectDao extends CreateDao<CreateProjectDto>, ReadDao<Project>, UpdateDao<Project>, DeleteDao {
}
