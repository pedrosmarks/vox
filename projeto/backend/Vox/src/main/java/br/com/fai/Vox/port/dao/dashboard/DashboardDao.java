package br.com.fai.Vox.port.dao.dashboard;

import br.com.fai.Vox.domain.dto.dashboard.DashboardOverviewDto;
import br.com.fai.Vox.domain.dto.dashboard.DateRangeFilter;
import br.com.fai.Vox.domain.dto.dashboard.HotspotDetailDto;
import br.com.fai.Vox.domain.dto.dashboard.MapPointDto;
import br.com.fai.Vox.domain.dto.dashboard.ModerationHealthDto;
import br.com.fai.Vox.domain.dto.dashboard.NeighborhoodHotspotDto;

import java.util.List;

/**
 * Consultas agregadas do painel administrativo. Todas as operações são
 * escopadas por município (municipalityId) e consideram apenas registros
 * aprovados na moderação quando fizer sentido para a visão pública.
 *
 * <p>O parâmetro {@link DateRangeFilter} restringe os resultados a um intervalo
 * de datas sobre {@code created_at}. Um filtro sem limites
 * ({@link DateRangeFilter#unbounded()}) equivale a não aplicar restrição.</p>
 */
public interface DashboardDao {

    DashboardOverviewDto getOverview(int municipalityId, DateRangeFilter dateRange);

    /**
     * Zonas quentes por coordenada: agrupa issues e projetos aprovados por
     * coordenada quantizada em uma grade (arredondamento por casas decimais).
     *
     * @param precision número de casas decimais usadas no arredondamento
     *                  (maior = células menores / mais granular)
     * @param dateRange intervalo de datas aplicado sobre {@code created_at}
     */
    List<MapPointDto> getMapHotspots(int municipalityId, int precision, DateRangeFilter dateRange);

    /** Zonas quentes agregadas por bairro. */
    List<NeighborhoodHotspotDto> getNeighborhoodHotspots(int municipalityId, DateRangeFilter dateRange);

    /** Detalhe (issues + projetos aprovados) de um bairro específico. */
    HotspotDetailDto getNeighborhoodDetail(int municipalityId, String neighborhood, DateRangeFilter dateRange);

    ModerationHealthDto getModerationHealth(int municipalityId, DateRangeFilter dateRange);
}
