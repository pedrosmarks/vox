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

/**
 * Painel administrativo. Todos os endpoints são restritos a ADMINISTRATOR e
 * sempre operam sobre o município do administrador autenticado (via token).
 *
 * <p>Todos os endpoints aceitam um filtro opcional de intervalo de datas
 * (sobre {@code created_at}) via query params {@code from} e {@code to}, no
 * formato ISO {@code yyyy-MM-dd}. O caso de uso principal é informar apenas
 * {@code from} (ex.: início do último mês/semana/bimestre) para trazer os
 * dados daquela data até hoje. Se {@code to} for omitido, assume-se hoje; se
 * ambos forem omitidos, nenhum filtro de data é aplicado.</p>
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
     * GET /api/admin/dashboard/overview?from=2026-08-16&to=2026-09-16
     * KPIs gerais do município (issues, projetos, usuários, taxas).
     */
    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewDto> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getOverview(municipalityId, from, to));
    }

    /**
     * GET /api/admin/dashboard/mapa/coordenadas?precision=3&from=2026-08-16
     * Zonas quentes por coordenada (heatmap), agrupadas em uma grade.
     */
    @GetMapping("/mapa/coordenadas")
    public ResponseEntity<List<MapPointDto>> getMapHotspots(
            @RequestParam(required = false) Integer precision,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getMapHotspots(municipalityId, precision, from, to));
    }

    /**
     * GET /api/admin/dashboard/mapa/bairros?from=2026-08-16
     * Zonas quentes agregadas por bairro.
     */
    @GetMapping("/mapa/bairros")
    public ResponseEntity<List<NeighborhoodHotspotDto>> getNeighborhoodHotspots(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getNeighborhoodHotspots(municipalityId, from, to));
    }

    /**
     * GET /api/admin/dashboard/mapa/bairros/detalhe?bairro=Centro&from=2026-08-16
     * Drill-down de uma zona: lista de issues e projetos daquele bairro.
     */
    @GetMapping("/mapa/bairros/detalhe")
    public ResponseEntity<HotspotDetailDto> getNeighborhoodDetail(
            @RequestParam("bairro") String bairro,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getNeighborhoodDetail(municipalityId, bairro, from, to));
    }

    /**
     * GET /api/admin/dashboard/moderacao?from=2026-08-16
     * Saúde da fila de moderação: backlog, taxas, tempo médio, ranking.
     */
    @GetMapping("/moderacao")
    public ResponseEntity<ModerationHealthDto> getModerationHealth(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getModerationHealth(municipalityId, from, to));
    }

    /**
     * GET /api/admin/dashboard/engajamento?from=2026-08-16
     * Engajamento cidadão: projetos mais apoiados/opinados, distribuição de
     * opiniões, ocorrências mais acompanhadas e usuários mais ativos.
     */
    @GetMapping("/engajamento")
    public ResponseEntity<EngagementDto> getEngagement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getEngagement(municipalityId, from, to));
    }

    /**
     * GET /api/admin/dashboard/categorias?from=2026-08-16
     * Análise por categoria: ranking e cruzamento categoria × status.
     */
    @GetMapping("/categorias")
    public ResponseEntity<CategoryAnalysisDto> getCategoryAnalysis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getCategoryAnalysis(municipalityId, from, to));
    }

    /**
     * GET /api/admin/dashboard/series-temporais?granularidade=month&from=2026-01-01
     * Volume de issues e projetos criados ao longo do tempo.
     * granularidade: day (padrão), week ou month.
     */
    @GetMapping("/series-temporais")
    public ResponseEntity<TimeSeriesDto> getTimeSeries(
            @RequestParam(value = "granularidade", required = false) String granularidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getTimeSeries(municipalityId, granularidade, from, to));
    }

    /**
     * GET /api/admin/dashboard/projetos/ciclo-vida?from=2026-08-16
     * Ciclo de vida dos projetos: distribuição por status, tempo médio por
     * etapa e execução orçamentária.
     */
    @GetMapping("/projetos/ciclo-vida")
    public ResponseEntity<ProjectLifecycleDto> getProjectLifecycle(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(dashboardService.getProjectLifecycle(municipalityId, from, to));
    }

    // --- Helpers ---

    private int requireAdminMunicipality(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        UserModel user = userService.findByid(userId);
        if (user == null || user.getRole() != UserRoleEnum.ADMINISTRATOR) {
            throw new SecurityException("Acesso negado: apenas administradores podem acessar o painel");
        }
        return authHelper.getMunicipalityId(request);
    }
}
