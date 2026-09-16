package br.com.fai.Vox.port.service.dashboard;

import br.com.fai.Vox.domain.dto.dashboard.DashboardOverviewDto;
import br.com.fai.Vox.domain.dto.dashboard.HotspotDetailDto;
import br.com.fai.Vox.domain.dto.dashboard.MapPointDto;
import br.com.fai.Vox.domain.dto.dashboard.ModerationHealthDto;
import br.com.fai.Vox.domain.dto.dashboard.NeighborhoodHotspotDto;

import java.util.List;

/**
 * Serviço do painel administrativo. Todas as operações são escopadas ao
 * município informado (obtido do token do administrador no controller).
 */
public interface DashboardService {

    DashboardOverviewDto getOverview(int municipalityId);

    List<MapPointDto> getMapHotspots(int municipalityId, Integer precision);

    List<NeighborhoodHotspotDto> getNeighborhoodHotspots(int municipalityId);

    HotspotDetailDto getNeighborhoodDetail(int municipalityId, String neighborhood);

    ModerationHealthDto getModerationHealth(int municipalityId);
}
