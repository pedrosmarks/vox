package br.com.fai.Vox.controller;
import br.com.fai.Vox.domain.enums.UserRoleEnum;

import br.com.fai.Vox.domain.Event;
import br.com.fai.Vox.domain.EventImage;
import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.CreateEventDto;
import br.com.fai.Vox.domain.dto.EventFilterDto;
import br.com.fai.Vox.domain.dto.PageResponse;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.event.EventService;
import br.com.fai.Vox.port.service.eventimage.EventImageService;
import br.com.fai.Vox.port.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CRUD de eventos. Leitura pública (qualquer usuário vê eventos de qualquer
 * município). Criação, edição e exclusão restritas a MODERATOR/ADMINISTRATOR.
 * Eventos não passam por moderação.
 */
@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private final EventService eventService;
    private final EventImageService eventImageService;
    private final UserService userService;
    private final AuthenticatedUserHelper authHelper;

    public EventRestController(EventService eventService,
                               EventImageService eventImageService,
                               UserService userService,
                               AuthenticatedUserHelper authHelper) {
        this.eventService = eventService;
        this.eventImageService = eventImageService;
        this.userService = userService;
        this.authHelper = authHelper;
    }

    // --- Leitura pública ---

    /**
     * GET /api/events — listagem pública paginada com filtros opcionais:
     * categoryId, municipalityId, search, minPrice, maxPrice, free, hasImage,
     * startFrom, startTo, page, size.
     */
    @GetMapping
    public ResponseEntity<PageResponse<Event>> find(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer municipalityId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean free,
            @RequestParam(required = false) Boolean hasImage,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        EventFilterDto filter = new EventFilterDto();
        filter.setCategoryId(categoryId);
        filter.setMunicipalityId(municipalityId);
        filter.setSearch(search);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setFree(free);
        filter.setHasImage(hasImage);
        filter.setStartFrom(startFrom);
        filter.setStartTo(startTo);
        filter.setPage(page);
        filter.setSize(size);

        return ResponseEntity.ok(eventService.find(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> findById(@PathVariable final int id) {
        Event event = eventService.findById(id);
        return event == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(event);
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<EventImage>> getImages(@PathVariable final int id) {
        return ResponseEntity.ok(eventImageService.findByEventId(id));
    }

    // --- Escrita (MODERATOR/ADMINISTRATOR) ---

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Void> create(@Valid @ModelAttribute final CreateEventDto data,
                                       MultipartHttpServletRequest request) {
        requireModeratorOrAdmin(request);
        data.setAuthorId(authHelper.getUserId(request));
        data.setMunicipalityId(authHelper.getMunicipalityId(request));
        data.setFile(request.getFile("file"));

        final int id = eventService.create(data);
        final URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Void> update(@PathVariable final int id,
                                       @ModelAttribute final CreateEventDto data,
                                       MultipartHttpServletRequest request) {
        requireModeratorOrAdmin(request);
        data.setFile(request.getFile("file"));
        eventService.update(id, data);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final int id, HttpServletRequest request) {
        requireModeratorOrAdmin(request);
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/images", consumes = {"multipart/form-data"})
    public ResponseEntity<Void> addImage(@PathVariable final int id,
                                         MultipartHttpServletRequest request) {
        requireModeratorOrAdmin(request);
        MultipartFile file = request.getFile("file");
        final int imageId = eventImageService.create(id, file);
        if (imageId < 0) return ResponseEntity.badRequest().build();

        final URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{imageId}").buildAndExpand(imageId).toUri();
        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable final int id,
                                            @PathVariable final int imageId,
                                            HttpServletRequest request) {
        requireModeratorOrAdmin(request);
        eventImageService.delete(imageId);
        return ResponseEntity.noContent().build();
    }

    private void requireModeratorOrAdmin(HttpServletRequest request) {
        UserModel user = userService.findByid(authHelper.getUserId(request));
        if (user == null || (user.getRole() != UserRoleEnum.MODERATOR
                && user.getRole() != UserRoleEnum.ADMINISTRATOR)) {
            throw new SecurityException("Acesso negado: apenas moderadores ou administradores");
        }
    }
}
