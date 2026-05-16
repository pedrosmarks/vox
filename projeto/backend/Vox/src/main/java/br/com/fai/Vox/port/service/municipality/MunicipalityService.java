package br.com.fai.Vox.port.service.municipality;

import br.com.fai.Vox.domain.Municipality;
import br.com.fai.Vox.port.service.crud.CreateService;
import br.com.fai.Vox.port.service.crud.ReadService;

public interface MunicipalityService extends ReadService<Municipality>, CreateService<Municipality> {

}
