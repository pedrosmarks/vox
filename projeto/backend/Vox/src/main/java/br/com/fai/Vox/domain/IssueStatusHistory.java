package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IssueStatusHistory {

    private Integer id;
    private Integer issueId;
    private IssueReport.IssueStatus previousStatus;
    private IssueReport.IssueStatus newStatus;
    private Integer changedBy;
    private String note;
    private LocalDateTime createdAt;

    public IssueStatusHistory() {}
}
