package br.com.fai.Vox.implementation.service.issuestatushistory;

import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.IssueStatusHistory;
import br.com.fai.Vox.port.dao.issuestatushistory.IssueStatusHistoryDao;
import br.com.fai.Vox.port.service.issuestatushistory.IssueStatusHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IssueStatusHistoryServiceImpl implements IssueStatusHistoryService {

    private final IssueStatusHistoryDao issueStatusHistoryDao;

    public IssueStatusHistoryServiceImpl(IssueStatusHistoryDao issueStatusHistoryDao) {
        this.issueStatusHistoryDao = issueStatusHistoryDao;
    }

    @Override
    public void recordStatusChange(int issueId, IssueReport.IssueStatus previousStatus, IssueReport.IssueStatus newStatus, int changedBy, String note) {
        IssueStatusHistory history = new IssueStatusHistory();
        history.setIssueId(issueId);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setNote(note);
        issueStatusHistoryDao.create(history);
    }

    @Override
    public List<IssueStatusHistory> findByIssueId(int issueId) {
        if (issueId <= 0) return List.of();
        return issueStatusHistoryDao.findByIssueId(issueId);
    }
}
