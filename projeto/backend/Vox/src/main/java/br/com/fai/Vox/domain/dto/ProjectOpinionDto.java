package br.com.fai.Vox.domain.dto;
import br.com.fai.Vox.domain.enums.OpinionTypeEnum;

import br.com.fai.Vox.domain.ProjectOpinion;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectOpinionDto {
    @NotNull(message = "Opinião é obrigatória")
    private OpinionTypeEnum opinion;

    public ProjectOpinionDto() {}
}
