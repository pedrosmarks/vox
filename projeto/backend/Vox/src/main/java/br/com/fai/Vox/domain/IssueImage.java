package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IssueImage {

    private Integer id;
    private Integer issueId;
    private String url;
    private LocalDateTime createdAt;

    public IssueImage() {}
}
