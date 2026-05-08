package br.com.fai.Vox.implementation.service.municipality;

import br.com.fai.Vox.domain.MunicipalityModel;
import br.com.fai.Vox.port.dao.municipality.MunicipalityDao;
import br.com.fai.Vox.port.service.municipality.MunicipalityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MunicipalityServiceImpl implements MunicipalityService {

    private final MunicipalityDao municipalityDao;

    public MunicipalityServiceImpl(MunicipalityDao municipalityDao) {
        this.municipalityDao = municipalityDao;
    }

    @Override
    public MunicipalityModel findByid(int id) {
        if (id < 0) {
            return null;
        }

        MunicipalityModel entity = municipalityDao.findByid(id);
        return entity;
    }

    @Override
    public List<MunicipalityModel> findAll() {
        final List<MunicipalityModel> entities = municipalityDao.findAll();
        return entities;
    }
}
