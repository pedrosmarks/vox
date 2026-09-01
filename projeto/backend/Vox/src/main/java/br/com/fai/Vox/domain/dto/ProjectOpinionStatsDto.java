package br.com.fai.Vox.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectOpinionStatsDto {

    private int total;
    private int approved;
    private int disapproved;
    private int neutral;

    public ProjectOpinionStatsDto() {}

    public ProjectOpinionStatsDto(int approved, int disapproved, int neutral) {
        this.approved = approved;
        this.disapproved = disapproved;
        this.neutral = neutral;
        this.total = approved + disapproved + neutral;
    }
}
