package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.CreateProjectDto;
import br.com.fai.Vox.port.service.project.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/project")
public class ProjectRestController {

    private final ProjectService projectService;

    public ProjectRestController(ProjectService projectService) {
        this.projectService = projectService;
    }


    @GetMapping()
    public ResponseEntity<List<Project>> getEntities() {
        List<Project> entities = projectService.findAll();

        return ResponseEntity.ok(entities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getEntityById(@PathVariable final int id) {
        Project entity = projectService.findByid(id);

        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable final int id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<CreateProjectDto> create(@ModelAttribute final CreateProjectDto data) {
        final int id = projectService.create(data);

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> update(@PathVariable final int id, @RequestBody final Project data) {

        projectService.update(id, data);

        return ResponseEntity.noContent().build();
    }
}
