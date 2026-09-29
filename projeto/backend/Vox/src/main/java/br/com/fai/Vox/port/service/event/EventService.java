package br.com.fai.Vox.port.service.event;

import br.com.fai.Vox.domain.Event;
import br.com.fai.Vox.domain.dto.CreateEventDto;
import br.com.fai.Vox.domain.dto.EventFilterDto;
import br.com.fai.Vox.domain.dto.PageResponse;

public interface EventService {

    int create(CreateEventDto dto);

    Event findById(int id);

    PageResponse<Event> find(EventFilterDto filter);

    void update(int id, CreateEventDto dto);

    void delete(int id);
}
