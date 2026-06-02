package br.com.fai.Vox.port.dao.municipality;

import br.com.fai.Vox.domain.Municipality;
import br.com.fai.Vox.port.dao.crud.CreateDao;
import br.com.fai.Vox.port.dao.crud.ReadDao;

public interface MunicipalityDao extends ReadDao<Municipality>, CreateDao<Municipality> {
}
