package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectSignature {

    private Integer id;
    private Integer projectId;
    private Integer userId;
    private LocalDateTime createdAt;

    public ProjectSignature() {}
}
