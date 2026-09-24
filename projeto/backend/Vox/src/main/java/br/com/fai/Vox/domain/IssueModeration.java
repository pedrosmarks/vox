package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import br.com.fai.Vox.domain.enums.ModerationStatusEnum;

@Getter
@Setter
public class IssueModeration {

    private Integer id;
    private Integer issueId;
    private Integer moderatorId;
    private ModerationStatusEnum action;
    private String feedback;
    private LocalDateTime createdAt;

    public IssueModeration() {}
}
