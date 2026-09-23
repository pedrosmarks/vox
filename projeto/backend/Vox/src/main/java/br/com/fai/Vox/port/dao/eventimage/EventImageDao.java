package br.com.fai.Vox.port.dao.eventimage;

import br.com.fai.Vox.domain.EventImage;

import java.util.List;

public interface EventImageDao {
    int create(EventImage entity);
    List<EventImage> findByEventId(int eventId);
    EventImage findById(int id);
    void delete(int id);
}
