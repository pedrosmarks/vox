package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.IssueImage;
import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.IssueStatusHistory;
import br.com.fai.Vox.domain.dto.CreateIssueReportDto;
import br.com.fai.Vox.domain.dto.UpdateIssueStatusDto;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.issueimage.IssueImageService;
import br.com.fai.Vox.port.service.issuereport.IssueReportService;
import br.com.fai.Vox.port.service.issuestatushistory.IssueStatusHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/issues")
public class IssueReportRestController {

    private final IssueReportService issueReportService;
    private final IssueImageService issueImageService;
    private final IssueStatusHistoryService issueStatusHistoryService;
    private final AuthenticatedUserHelper authHelper;

    public IssueReportRestController(IssueReportService issueReportService,
                                      IssueImageService issueImageService,
                                      IssueStatusHistoryService issueStatusHistoryService,
                                      AuthenticatedUserHelper authHelper) {
        this.issueReportService = issueReportService;
        this.issueImageService = issueImageService;
        this.issueStatusHistoryService = issueStatusHistoryService;
        this.authHelper = authHelper;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Void> create(
            @ModelAttribute final CreateIssueReportDto data,
            MultipartHttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        int municipalityId = authHelper.getMunicipalityId(request);
        data.setAuthorId(userId);
        data.setMunicipalityId(municipalityId);

        MultipartFile file = request.getFile("file");
        data.setFile(file);

        final int id = issueReportService.create(data);
        if (id < 0) return ResponseEntity.badRequest().build();

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping
    public ResponseEntity<List<IssueReport>> findAll(HttpServletRequest request) {
        int municipalityId = authHelper.getMunicipalityId(request);
        return ResponseEntity.ok(issueReportService.findByMunicipalityId(municipalityId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<IssueReport>> findMy(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        return ResponseEntity.ok(issueReportService.findByAuthorId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueReport> findById(@PathVariable final int id) {
        IssueReport entity = issueReportService.findByid(id);
        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable final int id,
                                        @RequestBody final IssueReport data,
                                        HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        issueReportService.update(id, data, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final int id) {
        issueReportService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- HISTÓRICO DE STATUS ---

    @GetMapping("/{id}/history")
    public ResponseEntity<List<IssueStatusHistory>> getHistory(@PathVariable final int id) {
        return ResponseEntity.ok(issueStatusHistoryService.findByIssueId(id));
    }

    // --- IMAGENS ---

    @PostMapping(value = "/{id}/images", consumes = {"multipart/form-data"})
    public ResponseEntity<Void> addImage(@PathVariable final int id,
                                          MultipartHttpServletRequest request) {
        MultipartFile file = request.getFile("file");
        final int imageId = issueImageService.create(id, file);
        if (imageId < 0) return ResponseEntity.badRequest().build();

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{imageId}")
                .buildAndExpand(imageId)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<IssueImage>> getImages(@PathVariable final int id) {
        return ResponseEntity.ok(issueImageService.findByIssueId(id));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable final int id,
                                             @PathVariable final int imageId) {
        issueImageService.delete(imageId);
        return ResponseEntity.noContent().build();
    }
}
