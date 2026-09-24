package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TimeSeriesDto {

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
