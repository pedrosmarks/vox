package br.com.fai.Vox.port.dao.issuemoderation;

import br.com.fai.Vox.domain.IssueModeration;

import java.util.List;

public interface IssueModerationDao {
    void create(IssueModeration entity);
    List<IssueModeration> findByIssueId(int issueId);
}
