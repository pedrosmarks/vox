package br.com.fai.Vox.implementation.service.event;

import br.com.fai.Vox.domain.Event;
import br.com.fai.Vox.domain.dto.CreateEventDto;
import br.com.fai.Vox.domain.dto.EventFilterDto;
import br.com.fai.Vox.domain.dto.PageResponse;
import br.com.fai.Vox.port.dao.event.EventDao;
import br.com.fai.Vox.port.dao.eventcategory.EventCategoryDao;
import br.com.fai.Vox.port.service.event.EventService;
import br.com.fai.Vox.port.service.eventimage.EventImageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EventServiceImpl implements EventService {

    private static final Logger logger = Logger.getLogger(EventServiceImpl.class.getName());

    private final EventDao eventDao;
    private final EventCategoryDao eventCategoryDao;
    private final EventImageService eventImageService;

    public EventServiceImpl(EventDao eventDao,
                            EventCategoryDao eventCategoryDao,
                            EventImageService eventImageService) {
        this.eventDao = eventDao;
        this.eventCategoryDao = eventCategoryDao;
        this.eventImageService = eventImageService;
    }

    @Override
    public int create(CreateEventDto dto) {
        validate(dto);

        Event entity = new Event();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setCategoryId(dto.getCategoryId());
        entity.setPrice(dto.getPrice());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setLocation(dto.getLocation());
        entity.setMunicipalityId(dto.getMunicipalityId());
        entity.setAuthorId(dto.getAuthorId());

        int eventId = eventDao.create(entity);
        logger.log(Level.INFO, "Evento criado. ID: " + eventId);

        // Imagem opcional na criação (imagens adicionais via sub-recurso).
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            eventImageService.create(eventId, dto.getFile());
        }
        return eventId;
    }

    @Override
    public Event findById(int id) {
        if (id <= 0) return null;
        Event event = eventDao.findById(id);
        if (event == null) return null;
        event.setImages(eventImageService.findByEventId(id));
        return event;
    }

    @Override
    public PageResponse<Event> find(EventFilterDto filter) {
        EventFilterDto f = filter != null ? filter : new EventFilterDto();
        List<Event> content = eventDao.find(f);
        long total = eventDao.count(f);
        return new PageResponse<>(content, f.getPage(), f.getSize(), total);
    }

    @Override
    public void update(int id, CreateEventDto dto) {
        if (id <= 0) return;
        Event existing = eventDao.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Evento não encontrado");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        if (dto.getCategoryId() != null) {
            requireCategory(dto.getCategoryId());
        }

        Event entity = new Event();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setCategoryId(dto.getCategoryId()); // null preserva a atual (COALESCE no DAO)
        entity.setPrice(dto.getPrice());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setLocation(dto.getLocation());

        eventDao.update(id, entity);
        logger.log(Level.INFO, "Evento atualizado. ID: " + id);

        // Se veio uma nova imagem no update, adiciona (não remove as existentes).
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            eventImageService.create(id, dto.getFile());
        }
    }

    @Override
    public void delete(int id) {
        if (id <= 0) return;
        // event_image tem ON DELETE CASCADE no banco: as imagens somem junto.
        eventDao.delete(id);
        logger.log(Level.INFO, "Evento removido. ID: " + id);
    }

    private void validate(CreateEventDto dto) {
        if (dto == null || dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
        requireCategory(dto.getCategoryId());
        if (dto.getStartDate() != null && dto.getEndDate() != null
                && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("A data de término não pode ser anterior à data de início");
        }
    }

    private void requireCategory(int categoryId) {
        if (eventCategoryDao.findById(categoryId) == null) {
            throw new IllegalArgumentException("Categoria de evento não encontrada");
        }
    }
}
