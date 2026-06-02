package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.IssueModeration;
import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.ProjectModeration;
import br.com.fai.Vox.domain.dto.ModerationActionDto;
import br.com.fai.Vox.domain.dto.UpdateIssueStatusDto;
import br.com.fai.Vox.domain.dto.UpdateProjectStatusDto;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.issuemoderation.IssueModerationService;
import br.com.fai.Vox.port.service.projectmoderation.ProjectModerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/moderation")
public class ModerationRestController {

    private final ProjectModerationService projectModerationService;
    private final IssueModerationService issueModerationService;
    private final AuthenticatedUserHelper authHelper;

    public ModerationRestController(ProjectModerationService projectModerationService,
                                     IssueModerationService issueModerationService,
                                     AuthenticatedUserHelper authHelper) {
        this.projectModerationService = projectModerationService;
        this.issueModerationService = issueModerationService;
        this.authHelper = authHelper;
    }

    // --- PROJETOS ---

    @GetMapping("/projects/pending")
    public ResponseEntity<List<Project>> findPendingProjects(HttpServletRequest request) {
        int municipalityId = authHelper.getMunicipalityId(request);
        return ResponseEntity.ok(projectModerationService.findPending(municipalityId));
    }

    @PostMapping("/projects/{id}/approve")
    public ResponseEntity<Void> approveProject(@PathVariable final int id,
                                                @RequestBody(required = false) final ModerationActionDto data,
                                                HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        String feedback = data != null ? data.getFeedback() : null;
        projectModerationService.approve(id, moderatorId, feedback);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/projects/{id}/reject")
    public ResponseEntity<Void> rejectProject(@PathVariable final int id,
                                               @RequestBody(required = false) final ModerationActionDto data,
                                               HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        String feedback = data != null ? data.getFeedback() : null;
        projectModerationService.reject(id, moderatorId, feedback);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/projects/{id}/status")
    public ResponseEntity<Void> updateProjectStatus(@PathVariable final int id,
                                                     @RequestBody final UpdateProjectStatusDto data,
                                                     HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        projectModerationService.updateStatus(id, data.getStatus(), moderatorId, data.getNote());
        return ResponseEntity.noContent().build();
    }

    // --- OCORRÊNCIAS ---

    @GetMapping("/issues/pending")
    public ResponseEntity<List<IssueReport>> findPendingIssues(HttpServletRequest request) {
        int municipalityId = authHelper.getMunicipalityId(request);
        return ResponseEntity.ok(issueModerationService.findPending(municipalityId));
    }

    @PostMapping("/issues/{id}/approve")
    public ResponseEntity<Void> approveIssue(@PathVariable final int id,
                                              @RequestBody(required = false) final ModerationActionDto data,
                                              HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        String feedback = data != null ? data.getFeedback() : null;
        issueModerationService.approve(id, moderatorId, feedback);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/issues/{id}/reject")
    public ResponseEntity<Void> rejectIssue(@PathVariable final int id,
                                             @RequestBody(required = false) final ModerationActionDto data,
                                             HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        String feedback = data != null ? data.getFeedback() : null;
        issueModerationService.reject(id, moderatorId, feedback);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/issues/{id}/status")
    public ResponseEntity<Void> updateIssueStatus(@PathVariable final int id,
                                                   @RequestBody final UpdateIssueStatusDto data,
                                                   HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        issueModerationService.updateStatus(id, data.getStatus(), moderatorId, data.getNote());
        return ResponseEntity.noContent().build();
    }
}
