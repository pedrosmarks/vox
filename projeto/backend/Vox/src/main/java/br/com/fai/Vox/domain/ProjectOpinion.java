package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectOpinion {

    private Integer id;
    private Integer projectId;
    private Integer userId;
    private OpinionType opinion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum OpinionType {
        APPROVE,
        DISAPPROVE,
        NEUTRAL
    }

    public ProjectOpinion() {}
}
