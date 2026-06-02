package br.com.fai.Vox.port.dao.issueimage;

import br.com.fai.Vox.domain.IssueImage;

import java.util.List;

public interface IssueImageDao {
    int create(IssueImage entity);
    void delete(int id);
    IssueImage findByid(int id);
    List<IssueImage> findByIssueId(int issueId);
}
