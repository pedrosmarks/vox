package br.com.fai.Vox.port.service.dashboard;

import br.com.fai.Vox.domain.dto.dashboard.DashboardOverviewDto;
import br.com.fai.Vox.domain.dto.dashboard.HotspotDetailDto;
import br.com.fai.Vox.domain.dto.dashboard.MapPointDto;
import br.com.fai.Vox.domain.dto.dashboard.ModerationHealthDto;
import br.com.fai.Vox.domain.dto.dashboard.NeighborhoodHotspotDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Serviço do painel administrativo. Todas as operações são escopadas ao
 * município informado (obtido do token do administrador no controller).
 *
 * <p>Os parâmetros {@code from} e {@code to} delimitam o intervalo de datas
 * (sobre {@code created_at}) considerado nas agregações. Ambos são opcionais:
 * o caso de uso típico é informar apenas {@code from} (ex.: início do último
 * mês/semana/bimestre) para buscar tudo daquela data até hoje. Quando ambos
 * são {@code null}, nenhum filtro de data é aplicado.</p>
 */
public interface DashboardService {

    DashboardOverviewDto getOverview(int municipalityId, LocalDate from, LocalDate to);

    List<MapPointDto> getMapHotspots(int municipalityId, Integer precision, LocalDate from, LocalDate to);

    List<NeighborhoodHotspotDto> getNeighborhoodHotspots(int municipalityId, LocalDate from, LocalDate to);

    HotspotDetailDto getNeighborhoodDetail(int municipalityId, String neighborhood, LocalDate from, LocalDate to);

    ModerationHealthDto getModerationHealth(int municipalityId, LocalDate from, LocalDate to);
}
