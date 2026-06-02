package br.com.fai.Vox.port.service.issuestatushistory;

import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.IssueStatusHistory;

import java.util.List;

public interface IssueStatusHistoryService {
    void recordStatusChange(int issueId, IssueReport.IssueStatus previousStatus, IssueReport.IssueStatus newStatus, int changedBy, String note);
    List<IssueStatusHistory> findByIssueId(int issueId);
}
