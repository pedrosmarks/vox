package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.ProjectImage;
import br.com.fai.Vox.port.service.projectimage.ProjectImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/project-image")
public class ProjectImageRestController {

    private final ProjectImageService projectImageService;

    public ProjectImageRestController(ProjectImageService projectImageService) {
        this.projectImageService = projectImageService;
    }


    @GetMapping()
    public ResponseEntity<List<ProjectImage>> getEntities() {
        List<ProjectImage> entities = projectImageService.findAll();

        return ResponseEntity.ok(entities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectImage> getEntityById(@PathVariable final int id) {
        ProjectImage entity = projectImageService.findByid(id);

        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable final int id) {
        projectImageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<ProjectImage> create(@RequestBody final ProjectImage data) {
        final int id = projectImageService.create(data);

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectImage> update(@PathVariable final int id, @RequestBody final ProjectImage data) {

        projectImageService.update(id, data);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<ProjectImage>> getEntityByRole(@PathVariable final int projectId) {
        final List<ProjectImage> entity = projectImageService.findByProjectId(projectId);
        if(entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(entity);
    }
}
