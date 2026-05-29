package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.ProjectImage;
import br.com.fai.Vox.port.service.projectimage.ProjectImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
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

    @GetMapping
    public ResponseEntity<List<ProjectImage>> getEntities() {
        return ResponseEntity.ok(projectImageService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectImage> getEntityById(@PathVariable final int id) {
        ProjectImage entity = projectImageService.findByid(id);
        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }

    @GetMapping("/project-id/{projectId}")
    public ResponseEntity<List<ProjectImage>> getByProjectId(@PathVariable final int projectId) {
        List<ProjectImage> entities = projectImageService.findByProjectId(projectId);
        return ResponseEntity.ok(entities);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Void> create(
            @RequestParam("projectId") final int projectId,
            MultipartHttpServletRequest request) {
        MultipartFile file = request.getFile("file");
        final int id = projectImageService.create(projectId, file);
        if (id < 0) return ResponseEntity.badRequest().build();

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final int id) {
        projectImageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Void> update(
            @PathVariable final int id,
            MultipartHttpServletRequest request) {
        MultipartFile file = request.getFile("file");
        projectImageService.update(id, file);
        return ResponseEntity.noContent().build();
    }
}
