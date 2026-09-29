package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NeighborhoodHotspotDto {

    private String neighborhood;
    private long issueCount;
    private long projectCount;
    private long total;

    public NeighborhoodHotspotDto() {}

    public NeighborhoodHotspotDto(String neighborhood, long issueCount, long projectCount) {
        this.neighborhood = neighborhood;
        this.issueCount = issueCount;
        this.projectCount = projectCount;
        this.total = issueCount + projectCount;
    }
}
