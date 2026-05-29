package br.com.fai.Vox.port.service.project;

import br.com.fai.Vox.domain.Project;

import java.util.List;

public interface ReadByMunicipalityIdService {
    List<Project> findByMunicipalityId(int municipalityId);
}
