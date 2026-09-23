package br.com.fai.Vox.implementation.service.eventcategory;

import br.com.fai.Vox.domain.EventCategory;
import br.com.fai.Vox.port.dao.eventcategory.EventCategoryDao;
import br.com.fai.Vox.port.service.eventcategory.EventCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventCategoryServiceImpl implements EventCategoryService {

    private final EventCategoryDao eventCategoryDao;

    public EventCategoryServiceImpl(EventCategoryDao eventCategoryDao) {
        this.eventCategoryDao = eventCategoryDao;
    }

    @Override
    public int create(EventCategory entity) {
        if (entity == null || entity.getName() == null || entity.getName().isBlank()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
        return eventCategoryDao.create(entity);
    }

    @Override
    public EventCategory findById(int id) {
        if (id <= 0) return null;
        return eventCategoryDao.findById(id);
    }

    @Override
    public List<EventCategory> findAll() {
        return eventCategoryDao.findAll();
    }

    @Override
    public void update(int id, EventCategory entity) {
        if (id <= 0) return;
        if (entity == null || entity.getName() == null || entity.getName().isBlank()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
        eventCategoryDao.update(id, entity);
    }

    @Override
    public void delete(int id) {
        if (id <= 0) return;
        eventCategoryDao.delete(id);
    }
}
