package br.com.fai.Vox.port.dao.issuestatushistory;

import br.com.fai.Vox.domain.IssueStatusHistory;

import java.util.List;

public interface IssueStatusHistoryDao {
    void create(IssueStatusHistory entity);
    List<IssueStatusHistory> findByIssueId(int issueId);
}
