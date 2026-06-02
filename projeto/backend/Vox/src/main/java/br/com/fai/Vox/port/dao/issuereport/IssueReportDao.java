package br.com.fai.Vox.port.dao.issuereport;

import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.dto.CreateIssueReportDto;
import br.com.fai.Vox.domain.enuns.ModerationStatus;

import java.util.List;

public interface IssueReportDao {
    int create(CreateIssueReportDto dto);
    void delete(int id);
    IssueReport findByid(int id);
    List<IssueReport> findAll();
    List<IssueReport> findByMunicipalityId(int municipalityId);
    List<IssueReport> findByAuthorId(int authorId);
    List<IssueReport> findByMunicipalityIdAndModerationStatus(int municipalityId, ModerationStatus moderationStatus);
    void update(int id, IssueReport entity);
    void updateModerationStatus(int id, ModerationStatus status);
    void updateStatus(int id, IssueReport.IssueStatus status);
}
