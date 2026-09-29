package br.com.fai.Vox.implementation.service.dashboard;

import br.com.fai.Vox.domain.dto.dashboard.CategoryAnalysisDto;
import br.com.fai.Vox.domain.dto.dashboard.DashboardOverviewDto;
import br.com.fai.Vox.domain.dto.dashboard.DateRangeFilter;
import br.com.fai.Vox.domain.dto.dashboard.EngagementDto;
import br.com.fai.Vox.domain.dto.dashboard.HotspotDetailDto;
import br.com.fai.Vox.domain.dto.dashboard.MapPointDto;
import br.com.fai.Vox.domain.dto.dashboard.ModerationHealthDto;
import br.com.fai.Vox.domain.dto.dashboard.NeighborhoodHotspotDto;
import br.com.fai.Vox.domain.dto.dashboard.ProjectLifecycleDto;
import br.com.fai.Vox.domain.dto.dashboard.TimeSeriesDto;
import br.com.fai.Vox.port.dao.dashboard.DashboardDao;
import br.com.fai.Vox.port.service.dashboard.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int DEFAULT_PRECISION = 3;
    private static final int MIN_PRECISION = 0;
    private static final int MAX_PRECISION = 6;

    private static final Set<String> ALLOWED_GRANULARITIES = Set.of("day", "week", "month");
    private static final String DEFAULT_GRANULARITY = "day";

    private final DashboardDao dashboardDao;

    public DashboardServiceImpl(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    @Override
    public DashboardOverviewDto getOverview(int municipalityId, LocalDate from, LocalDate to) {
        requireMunicipality(municipalityId);
        return dashboardDao.getOverview(municipalityId, buildDateRange(from, to));
    }

    @Override
    public List<MapPointDto> getMapHotspots(int municipalityId, Integer precision, LocalDate from, LocalDate to) {
        requireMunicipality(municipalityId);
        int p = precision == null ? DEFAULT_PRECISION : precision;
        if (p < MIN_PRECISION) p = MIN_PRECISION;
        if (p > MAX_PRECISION) p = MAX_PRECISION;
        return dashboardDao.getMapHotspots(municipalityId, p, buildDateRange(from, to));
    }

    @Override
    public List<NeighborhoodHotspotDto> getNeighborhoodHotspots(int municipalityId, LocalDate from, LocalDate to) {
        requireMunicipality(municipalityId);
        return dashboardDao.getNeighborhoodHotspots(municipalityId, buildDateRange(from, to));
    }

    @Override
    public HotspotDetailDto getNeighborhoodDetail(int municipalityId, String neighborhood, LocalDate from, LocalDate to) {
        requireMunicipality(municipalityId);
        if (neighborhood == null || neighborhood.trim().isEmpty()) {
            throw new IllegalArgumentException("Bairro é obrigatório");
        }
        return dashboardDao.getNeighborhoodDetail(municipalityId, neighborhood.trim(), buildDateRange(from, to));
    }

    @Override
    public ModerationHealthDto getModerationHealth(int municipalityId, LocalDate from, LocalDate to) {
        requireMunicipality(municipalityId);
        return dashboardDao.getModerationHealth(municipalityId, buildDateRange(from, to));
    }

    @Override
    public EngagementDto getEngagement(int municipalityId, LocalDate from, LocalDate to) {
        requireMunicipality(municipalityId);
        return dashboardDao.getEngagement(municipalityId, buildDateRange(from, to));
    }

    @Override
    public CategoryAnalysisDto getCategoryAnalysis(int municipalityId, LocalDate from, LocalDate to) {
        requireMunicipality(municipalityId);
        return dashboardDao.getCategoryAnalysis(municipalityId, buildDateRange(from, to));
    }

    @Override
    public TimeSeriesDto getTimeSeries(int municipalityId, String granularity, LocalDate from, LocalDate to) {
        requireMunicipality(municipalityId);
        return dashboardDao.getTimeSeries(municipalityId, normalizeGranularity(granularity), buildDateRange(from, to));
    }

    @Override
    public ProjectLifecycleDto getProjectLifecycle(int municipalityId, LocalDate from, LocalDate to) {
        requireMunicipality(municipalityId);
        return dashboardDao.getProjectLifecycle(municipalityId, buildDateRange(from, to));
    }

    private String normalizeGranularity(String granularity) {
        if (granularity == null || granularity.isBlank()) {
            return DEFAULT_GRANULARITY;
        }
        String normalized = granularity.trim().toLowerCase();
        if (!ALLOWED_GRANULARITIES.contains(normalized)) {
            throw new IllegalArgumentException("Granularidade inválida. Use: day, week ou month");
        }
        return normalized;
    }

    private DateRangeFilter buildDateRange(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return DateRangeFilter.unbounded();
        }
        LocalDate effectiveTo = to == null ? LocalDate.now() : to;
        return DateRangeFilter.of(from, effectiveTo);
    }

    private void requireMunicipality(int municipalityId) {
        if (municipalityId <= 0) {
            throw new IllegalArgumentException("Município inválido");
        }
    }
}
