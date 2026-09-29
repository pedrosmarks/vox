package br.com.fai.Vox.port.dao.issuereport;
import br.com.fai.Vox.domain.enums.IssueStatusEnum;

import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.dto.CreateIssueReportDto;
import br.com.fai.Vox.domain.enums.ModerationStatusEnum;

import java.util.List;

public interface IssueReportDao {
    int create(CreateIssueReportDto dto);
    void delete(int id);
    IssueReport findByid(int id);
    List<IssueReport> findAll();
    List<IssueReport> findByMunicipalityId(int municipalityId);
    List<IssueReport> findByAuthorId(int authorId);
    long countCreatedInLastWeek(int authorId);
    List<IssueReport> findByMunicipalityIdAndModerationStatus(int municipalityId, ModerationStatusEnum moderationStatus);
    List<IssueReport> findByMunicipalityIdAndModerationStatus(int municipalityId, ModerationStatusEnum moderationStatus, int limit, int offset);
    long countByMunicipalityIdAndModerationStatus(int municipalityId, ModerationStatusEnum moderationStatus);
    void update(int id, IssueReport entity);
    boolean assignCouncilor(int issueId, int councilorId, int municipalityId);
    boolean unassignCouncilor(int issueId, int councilorId, int municipalityId);
    void updateModerationStatus(int id, ModerationStatusEnum status);
    void updateStatus(int id, IssueStatusEnum status);
}
