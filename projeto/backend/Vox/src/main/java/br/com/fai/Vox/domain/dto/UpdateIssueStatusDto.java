package br.com.fai.Vox.domain.dto;
import br.com.fai.Vox.domain.enums.IssueStatusEnum;

import br.com.fai.Vox.domain.IssueReport;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateIssueStatusDto {

    private IssueStatusEnum status;
    private String note;
}
