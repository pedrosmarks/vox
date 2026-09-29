package br.com.fai.Vox.port.dao.dashboard;

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

import java.util.List;

public interface DashboardDao {

    DashboardOverviewDto getOverview(int municipalityId, DateRangeFilter dateRange);

    List<MapPointDto> getMapHotspots(int municipalityId, int precision, DateRangeFilter dateRange);

    List<NeighborhoodHotspotDto> getNeighborhoodHotspots(int municipalityId, DateRangeFilter dateRange);

    HotspotDetailDto getNeighborhoodDetail(int municipalityId, String neighborhood, DateRangeFilter dateRange);

    ModerationHealthDto getModerationHealth(int municipalityId, DateRangeFilter dateRange);

    EngagementDto getEngagement(int municipalityId, DateRangeFilter dateRange);

    CategoryAnalysisDto getCategoryAnalysis(int municipalityId, DateRangeFilter dateRange);

    TimeSeriesDto getTimeSeries(int municipalityId, String granularity, DateRangeFilter dateRange);

    ProjectLifecycleDto getProjectLifecycle(int municipalityId, DateRangeFilter dateRange);
}
