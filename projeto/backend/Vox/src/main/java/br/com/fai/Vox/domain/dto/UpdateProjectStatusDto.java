package br.com.fai.Vox.domain.dto;

import br.com.fai.Vox.domain.Project;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectStatusDto {

    private Project.ProjectStatus status;
    private String note;
}
