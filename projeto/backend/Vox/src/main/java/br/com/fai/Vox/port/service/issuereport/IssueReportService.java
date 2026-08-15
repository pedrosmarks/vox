package br.com.fai.Vox.port.service.issuereport;

import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.dto.CreateIssueReportDto;
import br.com.fai.Vox.domain.dto.PageResponse;

import java.util.List;

public interface IssueReportService {
    int create(CreateIssueReportDto dto);
    void delete(int id);
    IssueReport findByid(int id);
    List<IssueReport> findByMunicipalityId(int municipalityId);
    PageResponse<IssueReport> findByMunicipalityId(int municipalityId, int page, int size);
    List<IssueReport> findByAuthorId(int authorId);
    List<IssueReport> findPendingByMunicipalityId(int municipalityId);
    PageResponse<IssueReport> findPendingByMunicipalityId(int municipalityId, int page, int size);
    void update(int id, IssueReport entity, int changedBy);
    void updateStatus(int id, IssueReport.IssueStatus status, int changedBy, String note);
}
