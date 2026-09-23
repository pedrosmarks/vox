package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Séries temporais: volume de issues e projetos criados ao longo do tempo,
 * agrupados por dia, semana ou mês. Escopado por município e intervalo de datas.
 */
@Getter
@Setter
public class TimeSeriesDto {

    /** Granularidade aplicada: {@code day}, {@code week} ou {@code month}. */
    private String granularity;

    private List<TimeSeriesPointDto> points;

    public TimeSeriesDto() {}

    public TimeSeriesDto(String granularity, List<TimeSeriesPointDto> points) {
        this.granularity = granularity;
        this.points = points;
    }

    @Getter
    @Setter
    public static class TimeSeriesPointDto {
        /** Início do período (ISO {@code yyyy-MM-dd}). */
        private String period;
        private long issueCount;
        private long projectCount;
        private long total;

        public TimeSeriesPointDto() {}

        public TimeSeriesPointDto(String period, long issueCount, long projectCount) {
            this.period = period;
            this.issueCount = issueCount;
            this.projectCount = projectCount;
            this.total = issueCount + projectCount;
        }
    }
}
