package br.com.fai.Vox.port.service.issuemoderation;

import br.com.fai.Vox.domain.IssueModeration;
import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.dto.PageResponse;

import java.util.List;

public interface IssueModerationService {
    List<IssueReport> findPending(int municipalityId);
    PageResponse<IssueReport> findPending(int municipalityId, int page, int size);
    void approve(int issueId, int moderatorId, String feedback);
    void reject(int issueId, int moderatorId, String feedback);
    void updateStatus(int issueId, IssueReport.IssueStatus status, int moderatorId, String note);
    List<IssueModeration> findByIssueId(int issueId);
}
