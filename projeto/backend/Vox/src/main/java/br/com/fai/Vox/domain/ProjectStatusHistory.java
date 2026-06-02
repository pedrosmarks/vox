package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectStatusHistory {

    private Integer id;
    private Integer projectId;
    private Project.ProjectStatus previousStatus;
    private Project.ProjectStatus newStatus;
    private Integer changedBy;
    private String note;
    private LocalDateTime createdAt;

    public ProjectStatusHistory() {}
}
