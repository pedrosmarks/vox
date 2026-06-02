package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectCouncilor {

    private Integer id;
    private Integer projectId;
    private Integer councilorId;

    public ProjectCouncilor() {}
}
