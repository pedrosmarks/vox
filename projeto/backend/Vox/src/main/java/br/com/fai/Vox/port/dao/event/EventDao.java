package br.com.fai.Vox.port.dao.event;

import br.com.fai.Vox.domain.Event;
import br.com.fai.Vox.domain.dto.EventFilterDto;

import java.util.List;

public interface EventDao {
    int create(Event entity);
    Event findById(int id);
    void update(int id, Event entity);
    void delete(int id);

    List<Event> find(EventFilterDto filter);

    long count(EventFilterDto filter);
}
