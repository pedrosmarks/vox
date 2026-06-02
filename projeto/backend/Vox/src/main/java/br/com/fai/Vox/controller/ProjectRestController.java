package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.ProjectCouncilor;
import br.com.fai.Vox.domain.ProjectImage;
import br.com.fai.Vox.domain.ProjectOpinion;
import br.com.fai.Vox.domain.ProjectStatusHistory;
import br.com.fai.Vox.domain.dto.CreateProjectDto;
import br.com.fai.Vox.domain.dto.ProjectOpinionDto;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.project.ProjectService;
import br.com.fai.Vox.port.service.projectcouncilor.ProjectCouncilorService;
import br.com.fai.Vox.port.service.projectimage.ProjectImageService;
import br.com.fai.Vox.port.service.projectopinion.ProjectOpinionService;
import br.com.fai.Vox.port.service.projectstatushistory.ProjectStatusHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/project")
public class ProjectRestController {

    private final ProjectService projectService;
    private final ProjectImageService projectImageService;
    private final ProjectOpinionService projectOpinionService;
    private final ProjectCouncilorService projectCouncilorService;
    private final ProjectStatusHistoryService projectStatusHistoryService;
    private final AuthenticatedUserHelper authHelper;

    public ProjectRestController(ProjectService projectService,
                                  ProjectImageService projectImageService,
                                  ProjectOpinionService projectOpinionService,
                                  ProjectCouncilorService projectCouncilorService,
                                  ProjectStatusHistoryService projectStatusHistoryService,
                                  AuthenticatedUserHelper authHelper) {
        this.projectService = projectService;
        this.projectImageService = projectImageService;
        this.projectOpinionService = projectOpinionService;
        this.projectCouncilorService = projectCouncilorService;
        this.projectStatusHistoryService = projectStatusHistoryService;
        this.authHelper = authHelper;
    }

    // --- CRUD ---

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Void> create(
            @ModelAttribute final CreateProjectDto data,
            MultipartHttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        int municipalityId = authHelper.getMunicipalityId(request);
        data.setAuthorId(userId);
        data.setMunicipalityId(municipalityId);

        MultipartFile file = request.getFile("file");
        data.setFile(file);

        final int id = projectService.create(data);

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> findById(@PathVariable final int id) {
        Project entity = projectService.findByid(id);
        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable final int id,
                                        @RequestBody final Project data,
                                        HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        projectService.update(id, data, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final int id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- MEUS PROJETOS ---

    @GetMapping("/my")
    public ResponseEntity<List<Project>> findMy(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        return ResponseEntity.ok(projectService.findByAuthorId(userId));
    }

    // --- HISTÓRICO DE STATUS ---

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ProjectStatusHistory>> getHistory(@PathVariable final int id) {
        return ResponseEntity.ok(projectStatusHistoryService.findByProjectId(id));
    }

    // --- IMAGENS ---

    @PostMapping(value = "/{id}/image", consumes = {"multipart/form-data"})
    public ResponseEntity<Void> addImage(@PathVariable final int id,
                                          MultipartHttpServletRequest request) {
        MultipartFile file = request.getFile("file");
        final int imageId = projectImageService.create(id, file);
        if (imageId < 0) return ResponseEntity.badRequest().build();

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{imageId}")
                .buildAndExpand(imageId)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<List<ProjectImage>> getImages(@PathVariable final int id) {
        return ResponseEntity.ok(projectImageService.findByProjectId(id));
    }

    @DeleteMapping("/{id}/image/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable final int id,
                                             @PathVariable final int imageId) {
        projectImageService.delete(imageId);
        return ResponseEntity.noContent().build();
    }

    // --- OPINIÕES ---

    @PostMapping("/{id}/opinion")
    public ResponseEntity<Void> submitOpinion(@PathVariable final int id,
                                               @RequestBody final ProjectOpinionDto data,
                                               HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        projectOpinionService.submitOpinion(id, userId, data.getOpinion());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/opinion")
    public ResponseEntity<List<ProjectOpinion>> getOpinions(@PathVariable final int id) {
        return ResponseEntity.ok(projectOpinionService.findByProjectId(id));
    }

    @GetMapping("/{id}/opinion/me")
    public ResponseEntity<ProjectOpinion> getMyOpinion(@PathVariable final int id,
                                                        HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        ProjectOpinion opinion = projectOpinionService.findByProjectIdAndUserId(id, userId);
        return opinion == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(opinion);
    }

    // --- VEREADORES ---

    @PostMapping("/{projectId}/councilor/{councilorId}")
    public ResponseEntity<Void> addCouncilor(@PathVariable final int projectId,
                                              @PathVariable final int councilorId) {
        projectCouncilorService.add(projectId, councilorId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}/councilor/{councilorId}")
    public ResponseEntity<Void> removeCouncilor(@PathVariable final int projectId,
                                                 @PathVariable final int councilorId) {
        projectCouncilorService.remove(projectId, councilorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/councilor")
    public ResponseEntity<List<ProjectCouncilor>> getCouncilors(@PathVariable final int projectId) {
        return ResponseEntity.ok(projectCouncilorService.findByProjectId(projectId));
    }
}
