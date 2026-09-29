package br.com.fai.Vox.controller;
import br.com.fai.Vox.domain.enums.UserRoleEnum;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.dashboard.CategoryAnalysisDto;
import br.com.fai.Vox.domain.dto.dashboard.DashboardOverviewDto;
import br.com.fai.Vox.domain.dto.dashboard.EngagementDto;
import br.com.fai.Vox.domain.dto.dashboard.HotspotDetailDto;
import br.com.fai.Vox.domain.dto.dashboard.MapPointDto;
import br.com.fai.Vox.domain.dto.dashboard.ModerationHealthDto;
import br.com.fai.Vox.domain.dto.dashboard.NeighborhoodHotspotDto;
import br.com.fai.Vox.domain.dto.dashboard.ProjectLifecycleDto;
import br.com.fai.Vox.domain.dto.dashboard.TimeSeriesDto;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.dashboard.DashboardService;
import br.com.fai.Vox.port.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardRestController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final AuthenticatedUserHelper authHelper;

    public DashboardRestController(DashboardService dashboardService,
                                   UserService userService,
                                   AuthenticatedUserHelper authHelper) {
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.authHelper = authHelper;
    }

    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewDto> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getOverview(municipalityId, from, to));
    }

    @GetMapping("/mapa/coordenadas")
    public ResponseEntity<List<MapPointDto>> getMapHotspots(
            @RequestParam(required = false) Integer precision,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getMapHotspots(municipalityId, precision, from, to));
    }

    @GetMapping("/mapa/bairros")
    public ResponseEntity<List<NeighborhoodHotspotDto>> getNeighborhoodHotspots(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getNeighborhoodHotspots(municipalityId, from, to));
    }

    @GetMapping("/mapa/bairros/detalhe")
    public ResponseEntity<HotspotDetailDto> getNeighborhoodDetail(
            @RequestParam("bairro") String bairro,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getNeighborhoodDetail(municipalityId, bairro, from, to));
    }

    @GetMapping("/moderacao")
    public ResponseEntity<ModerationHealthDto> getModerationHealth(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getModerationHealth(municipalityId, from, to));
    }

    @GetMapping("/engajamento")
    public ResponseEntity<EngagementDto> getEngagement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getEngagement(municipalityId, from, to));
    }

    @GetMapping("/categorias")
    public ResponseEntity<CategoryAnalysisDto> getCategoryAnalysis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getCategoryAnalysis(municipalityId, from, to));
    }

    @GetMapping("/series-temporais")
    public ResponseEntity<TimeSeriesDto> getTimeSeries(
            @RequestParam(value = "granularidade", required = false) String granularidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getTimeSeries(municipalityId, granularidade, from, to));
    }

    @GetMapping("/projetos/ciclo-vida")
    public ResponseEntity<ProjectLifecycleDto> getProjectLifecycle(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getProjectLifecycle(municipalityId, from, to));
    }

    private int requireAdminMunicipality(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        UserModel user = userService.findByid(userId);
        if (user == null || user.getRole() != UserRoleEnum.ADMINISTRATOR) {
            throw new SecurityException("Acesso negado: apenas administradores podem acessar o painel");
        }
        return authHelper.getMunicipalityId(request);
    }
}
