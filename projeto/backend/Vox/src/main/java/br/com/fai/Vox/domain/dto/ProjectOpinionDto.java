package br.com.fai.Vox.domain.dto;

import br.com.fai.Vox.domain.ProjectOpinion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectOpinionDto {
    private ProjectOpinion.OpinionType opinion;

    public ProjectOpinionDto() {}
}
