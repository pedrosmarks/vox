package br.com.fai.Vox.port.service.event;

import br.com.fai.Vox.domain.Event;
import br.com.fai.Vox.domain.dto.CreateEventDto;
import br.com.fai.Vox.domain.dto.EventFilterDto;
import br.com.fai.Vox.domain.dto.PageResponse;

public interface EventService {

    int create(CreateEventDto dto);

    /** Retorna o evento com suas imagens carregadas, ou {@code null} se não existir. */
    Event findById(int id);

    /** Listagem pública paginada aplicando os filtros. */
    PageResponse<Event> find(EventFilterDto filter);

    void update(int id, CreateEventDto dto);

    void delete(int id);
}
