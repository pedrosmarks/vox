package br.com.fai.Vox.domain;

import br.com.fai.Vox.domain.enuns.ModerationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IssueModeration {

    private Integer id;
    private Integer issueId;
    private Integer moderatorId;
    private ModerationStatus action;
    private String feedback;
    private LocalDateTime createdAt;

    public IssueModeration() {}
}
