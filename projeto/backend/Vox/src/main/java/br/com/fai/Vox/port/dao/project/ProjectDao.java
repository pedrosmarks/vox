package br.com.fai.Vox.port.dao.project;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.dto.CreateProjectDto;
import br.com.fai.Vox.port.dao.crud.CreateDao;
import br.com.fai.Vox.port.dao.crud.DeleteDao;
import br.com.fai.Vox.port.dao.crud.ReadDao;
import br.com.fai.Vox.port.dao.crud.UpdateDao;

import java.util.List;

public interface ProjectDao extends CreateDao<CreateProjectDto>, ReadDao<Project>, UpdateDao<Project>, DeleteDao, ReadByMunicipalityIdDao {
    List<Project> findByStatus(Project.ProjectStatus status);
    List<Project> findByMunicipalityIdAndStatus(int municipalityId, Project.ProjectStatus status);
    void updateStatus(int id, Project.ProjectStatus status);
}
