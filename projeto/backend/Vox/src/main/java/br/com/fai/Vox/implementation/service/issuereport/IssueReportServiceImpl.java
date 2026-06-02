package br.com.fai.Vox.implementation.service.issuereport;

import br.com.fai.Vox.domain.IssueImage;
import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.Notification;
import br.com.fai.Vox.domain.Subscription;
import br.com.fai.Vox.domain.dto.CreateIssueReportDto;
import br.com.fai.Vox.domain.enuns.ModerationStatus;
import br.com.fai.Vox.port.dao.issuereport.IssueReportDao;
import br.com.fai.Vox.port.dao.issueimage.IssueImageDao;
import br.com.fai.Vox.port.service.drive.CloudinaryService;
import br.com.fai.Vox.port.service.issuereport.IssueReportService;
import br.com.fai.Vox.port.service.issuestatushistory.IssueStatusHistoryService;
import br.com.fai.Vox.port.service.notification.NotificationService;
import br.com.fai.Vox.port.service.subscription.SubscriptionService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class IssueReportServiceImpl implements IssueReportService {

    private static final Logger logger = Logger.getLogger(IssueReportServiceImpl.class.getName());

    private final IssueReportDao issueReportDao;
    private final IssueImageDao issueImageDao;
    private final CloudinaryService cloudinaryService;
    private final IssueStatusHistoryService issueStatusHistoryService;
    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;

    public IssueReportServiceImpl(IssueReportDao issueReportDao,
                                   IssueImageDao issueImageDao,
                                   CloudinaryService cloudinaryService,
                                   IssueStatusHistoryService issueStatusHistoryService,
                                   NotificationService notificationService,
                                   SubscriptionService subscriptionService) {
        this.issueReportDao = issueReportDao;
        this.issueImageDao = issueImageDao;
        this.cloudinaryService = cloudinaryService;
        this.issueStatusHistoryService = issueStatusHistoryService;
        this.notificationService = notificationService;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public int create(CreateIssueReportDto dto) {
        if (dto == null || dto.getTitle() == null || dto.getTitle().isEmpty()) return -1;

        final int issueId = issueReportDao.create(dto);
        logger.log(Level.INFO, "IssueReport criada. ID: " + issueId);

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            try {
                String fileName = "issue_" + issueId + "_" + dto.getFile().getOriginalFilename();
                String url = cloudinaryService.uploadFile(dto.getFile(), fileName);
                IssueImage image = new IssueImage();
                image.setIssueId(issueId);
                image.setUrl(url);
                issueImageDao.create(image);
                logger.log(Level.INFO, "Imagem salva para issue ID: " + issueId);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Erro ao processar imagem para issue ID: " + issueId, e);
            }
        }

        return issueId;
    }

    @Override
    public void delete(int id) {
        if (id <= 0) return;
        issueReportDao.delete(id);
    }

    @Override
    public IssueReport findByid(int id) {
        if (id <= 0) return null;
        return issueReportDao.findByid(id);
    }

    @Override
    public List<IssueReport> findByMunicipalityId(int municipalityId) {
        if (municipalityId <= 0) return List.of();
        // Cidadão e vereador só veem ocorrências aprovadas
        return issueReportDao.findByMunicipalityIdAndModerationStatus(municipalityId, ModerationStatus.APPROVED);
    }

    @Override
    public List<IssueReport> findByAuthorId(int authorId) {
        if (authorId <= 0) return List.of();
        return issueReportDao.findByAuthorId(authorId);
    }

    @Override
    public List<IssueReport> findPendingByMunicipalityId(int municipalityId) {
        if (municipalityId <= 0) return List.of();
        return issueReportDao.findByMunicipalityIdAndModerationStatus(municipalityId, ModerationStatus.PENDING);
    }

    @Override
    public void update(int id, IssueReport entity, int changedBy) {
        if (id <= 0) return;
        IssueReport existing = findByid(id);
        if (existing == null) return;

        if (existing.getStatus() != entity.getStatus()) {
            issueStatusHistoryService.recordStatusChange(
                    id, existing.getStatus(), entity.getStatus(), changedBy, null);

            // Notificar autor quando status muda
            notificationService.send(
                    existing.getAuthorId(),
                    "Ocorrência atualizada",
                    "O status da sua ocorrência \"" + existing.getTitle() + "\" foi alterado.",
                    Notification.NotificationType.ISSUE_STATUS_CHANGED);
        }

        issueReportDao.update(id, entity);

        // Notificar assinantes da ocorrência
        List<Integer> subscribers = subscriptionService.findSubscriberUserIds(Subscription.SubscriptionType.ISSUE, id);
        for (int userId : subscribers) {
            if (userId != changedBy) {
                notificationService.send(userId,
                        "Ocorrência atualizada",
                        "A ocorrência \"" + entity.getTitle() + "\" foi atualizada.",
                        Notification.NotificationType.ISSUE_UPDATED);
            }
        }
    }

    @Override
    public void updateStatus(int id, IssueReport.IssueStatus status, int changedBy, String note) {
        if (id <= 0 || status == null) return;
        IssueReport existing = findByid(id);
        if (existing == null) return;

        issueStatusHistoryService.recordStatusChange(id, existing.getStatus(), status, changedBy, note);
        issueReportDao.updateStatus(id, status);

        // Notificar autor
        notificationService.send(
                existing.getAuthorId(),
                "Status da ocorrência atualizado",
                "O status da sua ocorrência \"" + existing.getTitle() + "\" foi alterado para " + status.name() + ".",
                Notification.NotificationType.ISSUE_STATUS_CHANGED);
    }
}
