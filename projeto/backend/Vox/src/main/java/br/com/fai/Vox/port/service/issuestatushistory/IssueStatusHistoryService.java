package br.com.fai.Vox.port.service.issuestatushistory;
import br.com.fai.Vox.domain.enums.IssueStatusEnum;

import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.IssueStatusHistory;

import java.util.List;

public interface IssueStatusHistoryService {
    void recordStatusChange(int issueId, IssueStatusEnum previousStatus, IssueStatusEnum newStatus, int changedBy, String note);
    List<IssueStatusHistory> findByIssueId(int issueId);
}
