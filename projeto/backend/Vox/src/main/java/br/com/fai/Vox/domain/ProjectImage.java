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

//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public Integer getProjectId() {
//        return projectId;
//    }
//
//    public void setProjectId(Integer projectId) {
//        this.projectId = projectId;
//    }
//
//    public String getUrl() {
//        return url;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }
//
//    public LocalDateTime getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public LocalDateTime getUpdatedAt() {
//        return updatedAt;
//    }
//
//    public void setUpdatedAt(LocalDateTime updatedAt) {
//        this.updatedAt = updatedAt;
//    }
}
