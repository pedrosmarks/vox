package br.com.fai.Vox.port.dao.project;

import br.com.fai.Vox.domain.Project;

import java.util.List;

public interface ReadByMunicipalityIdDao {
    List<Project> findByMunicipalityId(int municipalityId);
}
