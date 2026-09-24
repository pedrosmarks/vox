package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import br.com.fai.Vox.domain.enums.IssueStatusEnum;

@Getter
@Setter
public class IssueStatusHistory {

    private Integer id;
    private Integer issueId;
    private IssueStatusEnum previousStatus;
    private IssueStatusEnum newStatus;
    private Integer changedBy;
    private String note;
    private LocalDateTime createdAt;

    public IssueStatusHistory() {}
}
