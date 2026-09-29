package br.com.fai.Vox.controller;
import br.com.fai.Vox.domain.enums.UserRoleEnum;

import br.com.fai.Vox.domain.EventCategory;
import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.eventcategory.EventCategoryService;
import br.com.fai.Vox.port.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/event-categories")
public class EventCategoryRestController {

    private final EventCategoryService eventCategoryService;
    private final UserService userService;
    private final AuthenticatedUserHelper authHelper;

    public EventCategoryRestController(EventCategoryService eventCategoryService,
                                       UserService userService,
                                       AuthenticatedUserHelper authHelper) {
        this.eventCategoryService = eventCategoryService;
        this.userService = userService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public ResponseEntity<List<EventCategory>> findAll() {
        return ResponseEntity.ok(eventCategoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventCategory> findById(@PathVariable final int id) {
        EventCategory entity = eventCategoryService.findById(id);
        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody final EventCategory data, HttpServletRequest request) {
        requireModeratorOrAdmin(request);
        final int id = eventCategoryService.create(data);
        final URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable final int id,
                                       @RequestBody final EventCategory data,
                                       HttpServletRequest request) {
        requireModeratorOrAdmin(request);
        eventCategoryService.update(id, data);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final int id, HttpServletRequest request) {
        requireModeratorOrAdmin(request);
        eventCategoryService.delete(id);
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
