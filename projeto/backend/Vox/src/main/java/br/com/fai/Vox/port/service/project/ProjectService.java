package br.com.fai.Vox.port.service.project;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.dto.CreateProjectDto;

import java.util.List;

public interface ProjectService {
    int create(CreateProjectDto dto);
    void delete(int id);
    Project findByid(int id);
    List<Project> findAll();
    List<Project> findByMunicipalityId(int municipalityId);
    List<Project> findByAuthorId(int authorId);
    void update(int id, Project entity, int changedBy);
}
