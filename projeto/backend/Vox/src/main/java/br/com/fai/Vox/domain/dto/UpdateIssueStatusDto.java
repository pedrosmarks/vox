package br.com.fai.Vox.domain.dto;

import br.com.fai.Vox.domain.IssueReport;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateIssueStatusDto {

    private IssueReport.IssueStatus status;
    private String note;
}
