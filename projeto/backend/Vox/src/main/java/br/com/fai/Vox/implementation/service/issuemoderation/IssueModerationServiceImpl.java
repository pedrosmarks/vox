package br.com.fai.Vox.implementation.service.issuemoderation;
import br.com.fai.Vox.domain.enums.NotificationTypeEnum;
import br.com.fai.Vox.domain.enums.IssueStatusEnum;
import br.com.fai.Vox.domain.enums.SubscriptionTypeEnum;

import br.com.fai.Vox.domain.IssueModeration;
import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.Notification;
import br.com.fai.Vox.domain.Subscription;
import br.com.fai.Vox.domain.dto.PageResponse;
import br.com.fai.Vox.domain.enums.ModerationStatusEnum;
import br.com.fai.Vox.port.dao.issuemoderation.IssueModerationDao;
import br.com.fai.Vox.port.dao.issuereport.IssueReportDao;
import br.com.fai.Vox.port.service.issuemoderation.IssueModerationService;
import br.com.fai.Vox.port.service.issuestatushistory.IssueStatusHistoryService;
import br.com.fai.Vox.port.service.notification.NotificationService;
import br.com.fai.Vox.port.service.subscription.SubscriptionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class IssueModerationServiceImpl implements IssueModerationService {

    private static final Logger logger = Logger.getLogger(IssueModerationServiceImpl.class.getName());

    private final IssueModerationDao issueModerationDao;
    private final IssueReportDao issueReportDao;
    private final IssueStatusHistoryService issueStatusHistoryService;
    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;

    public IssueModerationServiceImpl(IssueModerationDao issueModerationDao,
                                       IssueReportDao issueReportDao,
                                       IssueStatusHistoryService issueStatusHistoryService,
                                       NotificationService notificationService,
                                       SubscriptionService subscriptionService) {
        this.issueModerationDao = issueModerationDao;
        this.issueReportDao = issueReportDao;
        this.issueStatusHistoryService = issueStatusHistoryService;
        this.notificationService = notificationService;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public List<IssueReport> findPending(int municipalityId) {
        if (municipalityId <= 0) return List.of();
        return issueReportDao.findByMunicipalityIdAndModerationStatus(municipalityId, ModerationStatusEnum.PENDING);
    }

    @Override
    public PageResponse<IssueReport> findPending(int municipalityId, int page, int size) {
        if (municipalityId <= 0) return new PageResponse<>(List.of(), page, size, 0);
        int offset = page * size;
        List<IssueReport> content = issueReportDao.findByMunicipalityIdAndModerationStatus(municipalityId, ModerationStatusEnum.PENDING, size, offset);
        long total = issueReportDao.countByMunicipalityIdAndModerationStatus(municipalityId, ModerationStatusEnum.PENDING);
        return new PageResponse<>(content, page, size, total);
    }

    @Override
    public void approve(int issueId, int moderatorId, String feedback) {
        if (issueId <= 0 || moderatorId <= 0) return;

        IssueReport issue = issueReportDao.findByid(issueId);
        if (issue == null) return;

        issueReportDao.updateModerationStatus(issueId, ModerationStatusEnum.APPROVED);

        issueStatusHistoryService.recordStatusChange(
                issueId, issue.getStatus(), issue.getStatus(), moderatorId, feedback);

        IssueModeration moderation = new IssueModeration();
        moderation.setIssueId(issueId);
        moderation.setModeratorId(moderatorId);
        moderation.setAction(ModerationStatusEnum.APPROVED);
        moderation.setFeedback(feedback);
        issueModerationDao.create(moderation);

        logger.log(Level.INFO, "Issue aprovada. ID: " + issueId);

        if (issue.getCouncilorId() != null) {
            notificationService.send(
                    issue.getCouncilorId(),
                    "Nova ocorrência atribuída a você",
                    "Uma nova ocorrência \"" + issue.getTitle() + "\" foi aprovada e está atribuída a você.",
                    NotificationTypeEnum.ISSUE_TAGGED);
        }

        List<Integer> allIssueSubscribers = subscriptionService.findSubscriberUserIds(
                SubscriptionTypeEnum.ALL_ISSUES, null);
        for (int userId : allIssueSubscribers) {
            notificationService.send(userId,
                    "Nova ocorrência publicada",
                    "Uma nova ocorrência \"" + issue.getTitle() + "\" está disponível.",
                    NotificationTypeEnum.ISSUE_CREATED);
        }
    }

    @Override
    public void reject(int issueId, int moderatorId, String feedback) {
        if (issueId <= 0 || moderatorId <= 0) return;

        IssueReport issue = issueReportDao.findByid(issueId);
        if (issue == null) return;

        IssueStatusEnum previousStatus = issue.getStatus();
        issueReportDao.updateModerationStatus(issueId, ModerationStatusEnum.REJECTED);
        issueReportDao.updateStatus(issueId, IssueStatusEnum.REJECTED);

        issueStatusHistoryService.recordStatusChange(
                issueId, previousStatus, IssueStatusEnum.REJECTED, moderatorId, feedback);

        IssueModeration moderation = new IssueModeration();
        moderation.setIssueId(issueId);
        moderation.setModeratorId(moderatorId);
        moderation.setAction(ModerationStatusEnum.REJECTED);
        moderation.setFeedback(feedback);
        issueModerationDao.create(moderation);

        logger.log(Level.INFO, "Issue rejeitada. ID: " + issueId);
    }

    @Override
    public void updateStatus(int issueId, IssueStatusEnum status, int moderatorId, String note) {
        if (issueId <= 0 || status == null) return;

        IssueReport existing = issueReportDao.findByid(issueId);
        if (existing == null) return;

        issueStatusHistoryService.recordStatusChange(issueId, existing.getStatus(), status, moderatorId, note);
        issueReportDao.updateStatus(issueId, status);

        notificationService.send(
                existing.getAuthorId(),
                "Status da ocorrência atualizado",
                "O status da sua ocorrência \"" + existing.getTitle() + "\" foi alterado para " + status.name() + ".",
                NotificationTypeEnum.ISSUE_STATUS_CHANGED);
    }

    @Override
    public List<IssueModeration> findByIssueId(int issueId) {
        if (issueId <= 0) return List.of();
        return issueModerationDao.findByIssueId(issueId);
    }
}
