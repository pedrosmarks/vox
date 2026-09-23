package br.com.fai.Vox.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectOpinionStatsDto {

    private int total;
    private int approved;
    private int neutral;

    public ProjectOpinionStatsDto() {}

    public ProjectOpinionStatsDto(int approved, int neutral) {
        this.approved = approved;
        this.neutral = neutral;
        this.total = approved + neutral;
    }
}
