package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectImage {

    private Integer id;
    private Integer projectId;
    private String url;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectImage() {
    }

    public ProjectImage(Integer id, Integer projectId, String url, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.url = url;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
