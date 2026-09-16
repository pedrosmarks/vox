package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.dashboard.DashboardOverviewDto;
import br.com.fai.Vox.domain.dto.dashboard.HotspotDetailDto;
import br.com.fai.Vox.domain.dto.dashboard.MapPointDto;
import br.com.fai.Vox.domain.dto.dashboard.ModerationHealthDto;
import br.com.fai.Vox.domain.dto.dashboard.NeighborhoodHotspotDto;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.dashboard.DashboardService;
import br.com.fai.Vox.port.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Painel administrativo. Todos os endpoints são restritos a ADMINISTRATOR e
 * sempre operam sobre o município do administrador autenticado (via token).
 */
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

    /**
     * GET /api/admin/dashboard/overview
     * KPIs gerais do município (issues, projetos, usuários, taxas).
     */
    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewDto> getOverview(HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getOverview(municipalityId));
    }

    /**
     * GET /api/admin/dashboard/mapa/coordenadas?precision=3
     * Zonas quentes por coordenada (heatmap), agrupadas em uma grade.
     */
    @GetMapping("/mapa/coordenadas")
    public ResponseEntity<List<MapPointDto>> getMapHotspots(
            @RequestParam(required = false) Integer precision,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getMapHotspots(municipalityId, precision));
    }

    /**
     * GET /api/admin/dashboard/mapa/bairros
     * Zonas quentes agregadas por bairro.
     */
    @GetMapping("/mapa/bairros")
    public ResponseEntity<List<NeighborhoodHotspotDto>> getNeighborhoodHotspots(HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getNeighborhoodHotspots(municipalityId));
    }

    /**
     * GET /api/admin/dashboard/mapa/bairros/detalhe?bairro=Centro
     * Drill-down de uma zona: lista de issues e projetos daquele bairro.
     */
    @GetMapping("/mapa/bairros/detalhe")
    public ResponseEntity<HotspotDetailDto> getNeighborhoodDetail(
            @RequestParam("bairro") String bairro,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getNeighborhoodDetail(municipalityId, bairro));
    }

    /**
     * GET /api/admin/dashboard/moderacao
     * Saúde da fila de moderação: backlog, taxas, tempo médio, ranking.
     */
    @GetMapping("/moderacao")
    public ResponseEntity<ModerationHealthDto> getModerationHealth(HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getModerationHealth(municipalityId));
    }

    // --- Helpers ---

    private int requireAdminMunicipality(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        UserModel user = userService.findByid(userId);
        if (user == null || user.getRole() != UserModel.UserRole.ADMINISTRATOR) {
            throw new SecurityException("Acesso negado: apenas administradores podem acessar o painel");
        }
        return authHelper.getMunicipalityId(request);
    }
}
