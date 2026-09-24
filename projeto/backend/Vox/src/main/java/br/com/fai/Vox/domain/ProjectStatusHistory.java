package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import br.com.fai.Vox.domain.enums.ProjectStatusEnum;

@Getter
@Setter
public class ProjectStatusHistory {

    private Integer id;
    private Integer projectId;
    private ProjectStatusEnum previousStatus;
    private ProjectStatusEnum newStatus;
    private Integer changedBy;
    private String note;
    private LocalDateTime createdAt;

    public ProjectStatusHistory() {}
}
