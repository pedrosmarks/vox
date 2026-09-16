package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

/**
 * Zona quente agregada por bairro (neighborhood). Cobre registros que
 * possuem bairro informado, agrupando issues e projetos por nome de bairro.
 */
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
