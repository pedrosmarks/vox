package br.com.fai.Vox.port.dao.eventcategory;

import br.com.fai.Vox.domain.EventCategory;

import java.util.List;

public interface EventCategoryDao {
    int create(EventCategory entity);
    EventCategory findById(int id);
    List<EventCategory> findAll();
    void update(int id, EventCategory entity);
    void delete(int id);
}
