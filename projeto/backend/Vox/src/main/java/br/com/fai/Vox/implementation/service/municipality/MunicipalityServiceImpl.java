package br.com.fai.Vox.implementation.service.municipality;

import br.com.fai.Vox.domain.Municipality;
import br.com.fai.Vox.domain.UserModel;
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
    public int create(Municipality entity) {
        int invalidResponse = -1;

        if (entity == null) {
            return invalidResponse;
        }

        if (entity.getName().isEmpty() || entity.getState().isEmpty()) {
            return invalidResponse;
        }

        final int id = municipalityDao.create(entity);
        return id;
    }

    @Override
    public Municipality findByid(int id) {
        if (id < 0) {
            return null;
        }

        Municipality entity = municipalityDao.findByid(id);
        return entity;
    }

    @Override
    public List<Municipality> findAll() {
        final List<Municipality> entities = municipalityDao.findAll();
        return entities;
    }
}
