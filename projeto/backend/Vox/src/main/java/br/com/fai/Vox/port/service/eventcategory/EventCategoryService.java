package br.com.fai.Vox.port.service.eventcategory;

import br.com.fai.Vox.domain.EventCategory;

import java.util.List;

public interface EventCategoryService {
    int create(EventCategory entity);
    EventCategory findById(int id);
    List<EventCategory> findAll();
    void update(int id, EventCategory entity);
    void delete(int id);
}
