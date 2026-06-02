package br.com.fai.Vox.domain;

import br.com.fai.Vox.domain.enuns.ModerationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectModeration {

    private Integer id;
    private Integer projectId;
    private Integer moderatorId;
    private ModerationStatus action;
    private String feedback;
    private LocalDateTime createdAt;

    public ProjectModeration() {}
}
