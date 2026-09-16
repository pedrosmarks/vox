package br.com.fai.Vox.port.dao.dashboard;

import br.com.fai.Vox.domain.dto.dashboard.DashboardOverviewDto;
import br.com.fai.Vox.domain.dto.dashboard.HotspotDetailDto;
import br.com.fai.Vox.domain.dto.dashboard.MapPointDto;
import br.com.fai.Vox.domain.dto.dashboard.ModerationHealthDto;
import br.com.fai.Vox.domain.dto.dashboard.NeighborhoodHotspotDto;

import java.util.List;

/**
 * Consultas agregadas do painel administrativo. Todas as operações são
 * escopadas por município (municipalityId) e consideram apenas registros
 * aprovados na moderação quando fizer sentido para a visão pública.
 */
public interface DashboardDao {

    DashboardOverviewDto getOverview(int municipalityId);

    /**
     * Zonas quentes por coordenada: agrupa issues e projetos aprovados por
     * coordenada quantizada em uma grade (arredondamento por casas decimais).
     *
     * @param precision número de casas decimais usadas no arredondamento
     *                  (maior = células menores / mais granular)
     */
    List<MapPointDto> getMapHotspots(int municipalityId, int precision);

    /** Zonas quentes agregadas por bairro. */
    List<NeighborhoodHotspotDto> getNeighborhoodHotspots(int municipalityId);

    /** Detalhe (issues + projetos aprovados) de um bairro específico. */
    HotspotDetailDto getNeighborhoodDetail(int municipalityId, String neighborhood);

    ModerationHealthDto getModerationHealth(int municipalityId);
}
