package br.com.fai.Vox.port.service.dashboard;

import br.com.fai.Vox.domain.dto.dashboard.CategoryAnalysisDto;
import br.com.fai.Vox.domain.dto.dashboard.DashboardOverviewDto;
import br.com.fai.Vox.domain.dto.dashboard.EngagementDto;
import br.com.fai.Vox.domain.dto.dashboard.HotspotDetailDto;
import br.com.fai.Vox.domain.dto.dashboard.MapPointDto;
import br.com.fai.Vox.domain.dto.dashboard.ModerationHealthDto;
import br.com.fai.Vox.domain.dto.dashboard.NeighborhoodHotspotDto;
import br.com.fai.Vox.domain.dto.dashboard.ProjectLifecycleDto;
import br.com.fai.Vox.domain.dto.dashboard.TimeSeriesDto;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardOverviewDto getOverview(int municipalityId, LocalDate from, LocalDate to);

    List<MapPointDto> getMapHotspots(int municipalityId, Integer precision, LocalDate from, LocalDate to);

    List<NeighborhoodHotspotDto> getNeighborhoodHotspots(int municipalityId, LocalDate from, LocalDate to);

    HotspotDetailDto getNeighborhoodDetail(int municipalityId, String neighborhood, LocalDate from, LocalDate to);

    ModerationHealthDto getModerationHealth(int municipalityId, LocalDate from, LocalDate to);

    EngagementDto getEngagement(int municipalityId, LocalDate from, LocalDate to);

    CategoryAnalysisDto getCategoryAnalysis(int municipalityId, LocalDate from, LocalDate to);

    TimeSeriesDto getTimeSeries(int municipalityId, String granularity, LocalDate from, LocalDate to);

    ProjectLifecycleDto getProjectLifecycle(int municipalityId, LocalDate from, LocalDate to);
}
