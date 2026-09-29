package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import br.com.fai.Vox.domain.enums.OpinionTypeEnum;

@Getter
@Setter
public class ProjectOpinion {

    private Integer id;
    private Integer projectId;
    private Integer userId;
    private OpinionTypeEnum opinion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectOpinion() {}
}
