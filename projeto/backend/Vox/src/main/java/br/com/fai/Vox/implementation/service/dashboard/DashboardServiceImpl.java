package br.com.fai.Vox.implementation.service.dashboard;

import br.com.fai.Vox.domain.dto.dashboard.DashboardOverviewDto;
import br.com.fai.Vox.domain.dto.dashboard.HotspotDetailDto;
import br.com.fai.Vox.domain.dto.dashboard.MapPointDto;
import br.com.fai.Vox.domain.dto.dashboard.ModerationHealthDto;
import br.com.fai.Vox.domain.dto.dashboard.NeighborhoodHotspotDto;
import br.com.fai.Vox.port.dao.dashboard.DashboardDao;
import br.com.fai.Vox.port.service.dashboard.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    /** Precisão padrão do arredondamento das coordenadas (~1km por célula). */
    private static final int DEFAULT_PRECISION = 3;
    private static final int MIN_PRECISION = 0;
    private static final int MAX_PRECISION = 6;

    private final DashboardDao dashboardDao;

    public DashboardServiceImpl(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    @Override
    public DashboardOverviewDto getOverview(int municipalityId) {
        requireMunicipality(municipalityId);
        return dashboardDao.getOverview(municipalityId);
    }

    @Override
    public List<MapPointDto> getMapHotspots(int municipalityId, Integer precision) {
        requireMunicipality(municipalityId);
        int p = precision == null ? DEFAULT_PRECISION : precision;
        if (p < MIN_PRECISION) p = MIN_PRECISION;
        if (p > MAX_PRECISION) p = MAX_PRECISION;
        return dashboardDao.getMapHotspots(municipalityId, p);
    }

    @Override
    public List<NeighborhoodHotspotDto> getNeighborhoodHotspots(int municipalityId) {
        requireMunicipality(municipalityId);
        return dashboardDao.getNeighborhoodHotspots(municipalityId);
    }

    @Override
    public HotspotDetailDto getNeighborhoodDetail(int municipalityId, String neighborhood) {
        requireMunicipality(municipalityId);
        if (neighborhood == null || neighborhood.trim().isEmpty()) {
            throw new IllegalArgumentException("Bairro é obrigatório");
        }
        return dashboardDao.getNeighborhoodDetail(municipalityId, neighborhood.trim());
    }

    @Override
    public ModerationHealthDto getModerationHealth(int municipalityId) {
        requireMunicipality(municipalityId);
        return dashboardDao.getModerationHealth(municipalityId);
    }

    private void requireMunicipality(int municipalityId) {
        if (municipalityId <= 0) {
            throw new IllegalArgumentException("Município inválido");
        }
    }
}
