package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.dto.CreateProjectDto;
import br.com.fai.Vox.implementation.service.authentication.jwt.JwtService;
import br.com.fai.Vox.port.service.project.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/project")
public class ProjectRestController {

    private final ProjectService projectService;
    private final JwtService jwtService;

    public ProjectRestController(ProjectService projectService, JwtService jwtService) {
        this.projectService = projectService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> getEntities(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            int municipalityId = jwtService.getMunicipalityIdFromToken(token);
            return ResponseEntity.ok(projectService.findByMunicipalityId(municipalityId));
        }
        return ResponseEntity.ok(projectService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getEntityById(@PathVariable final int id) {
        Project entity = projectService.findByid(id);
        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final int id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Void> create(
            @ModelAttribute final CreateProjectDto data,
            MultipartHttpServletRequest request) {
        MultipartFile file = request.getFile("file");
        System.out.println("FILE NO CONTROLLER: " + (file != null ? file.getOriginalFilename() + " size=" + file.getSize() : "NULL"));
        data.setFile(file);
        final int id = projectService.create(data);

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable final int id, @RequestBody final Project data) {
        projectService.update(id, data);
        return ResponseEntity.noContent().build();
    }
}
