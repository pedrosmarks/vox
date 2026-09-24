package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MapPointDto {

    private BigDecimal latitude;
    private BigDecimal longitude;
    private long issueCount;
    private long projectCount;
    private long total;

    public MapPointDto() {}

    public MapPointDto(BigDecimal latitude, BigDecimal longitude,
                       long issueCount, long projectCount) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.issueCount = issueCount;
        this.projectCount = projectCount;
        this.total = issueCount + projectCount;
    }
}
