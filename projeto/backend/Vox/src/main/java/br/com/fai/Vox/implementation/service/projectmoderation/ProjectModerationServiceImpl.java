package br.com.fai.Vox.implementation.service.projectmoderation;
import br.com.fai.Vox.domain.enums.ProjectStatusEnum;
import br.com.fai.Vox.domain.enums.NotificationTypeEnum;
import br.com.fai.Vox.domain.enums.SubscriptionTypeEnum;

import br.com.fai.Vox.domain.Notification;
import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.ProjectModeration;
import br.com.fai.Vox.domain.Subscription;
import br.com.fai.Vox.domain.dto.PageResponse;
import br.com.fai.Vox.domain.enums.ModerationStatusEnum;
import br.com.fai.Vox.port.dao.project.ProjectDao;
import br.com.fai.Vox.port.dao.projectmoderation.ProjectModerationDao;
import br.com.fai.Vox.port.service.notification.NotificationService;
import br.com.fai.Vox.port.service.projectmoderation.ProjectModerationService;
import br.com.fai.Vox.port.service.projectstatushistory.ProjectStatusHistoryService;
import br.com.fai.Vox.port.service.subscription.SubscriptionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ProjectModerationServiceImpl implements ProjectModerationService {

    private static final Logger logger = Logger.getLogger(ProjectModerationServiceImpl.class.getName());

    private final ProjectModerationDao projectModerationDao;
    private final ProjectDao projectDao;
    private final ProjectStatusHistoryService projectStatusHistoryService;
    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;

    public ProjectModerationServiceImpl(ProjectModerationDao projectModerationDao,
                                         ProjectDao projectDao,
                                         ProjectStatusHistoryService projectStatusHistoryService,
                                         NotificationService notificationService,
                                         SubscriptionService subscriptionService) {
        this.projectModerationDao = projectModerationDao;
        this.projectDao = projectDao;
        this.projectStatusHistoryService = projectStatusHistoryService;
        this.notificationService = notificationService;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public List<Project> findPending(int municipalityId) {
        if (municipalityId <= 0) return List.of();
        return projectDao.findByMunicipalityId(municipalityId).stream()
                .filter(p -> ModerationStatusEnum.PENDING.equals(p.getModerationStatus()))
                .toList();
    }

    @Override
    public PageResponse<Project> findPending(int municipalityId, int page, int size) {
        if (municipalityId <= 0) return new PageResponse<>(List.of(), page, size, 0);
        List<Project> all = findPending(municipalityId);
        long total = all.size();
        int offset = page * size;
        List<Project> content = all.stream().skip(offset).limit(size).toList();
        return new PageResponse<>(content, page, size, total);
    }

    @Override
    public void approve(int projectId, int moderatorId, String feedback) {
        if (projectId <= 0 || moderatorId <= 0) return;

        Project project = projectDao.findByid(projectId);
        if (project == null) return;

        ProjectStatusEnum previousStatus = project.getStatus();
        project.setModerationStatus(ModerationStatusEnum.APPROVED);
        project.setStatus(ProjectStatusEnum.PUBLISHED);
        projectDao.update(projectId, project);

        projectStatusHistoryService.recordStatusChange(
                projectId, previousStatus, ProjectStatusEnum.PUBLISHED, moderatorId, feedback);

        ProjectModeration moderation = new ProjectModeration();
        moderation.setProjectId(projectId);
        moderation.setModeratorId(moderatorId);
        moderation.setAction(ModerationStatusEnum.APPROVED);
        moderation.setFeedback(feedback);
        projectModerationDao.create(moderation);

        logger.log(Level.INFO, "Projeto aprovado. ID: " + projectId);

        notificationService.send(project.getAuthorId(),
                "Projeto aprovado",
                "Seu projeto \"" + project.getTitle() + "\" foi aprovado.",
                NotificationTypeEnum.PROJECT_STATUS_CHANGED);

        List<Integer> allSubs = subscriptionService.findSubscriberUserIds(
                SubscriptionTypeEnum.ALL_PROJECTS, null);
        for (int userId : allSubs) {
            notificationService.send(userId,
                    "Novo projeto publicado",
                    "O projeto \"" + project.getTitle() + "\" está disponível.",
                    NotificationTypeEnum.PROJECT_CREATED);
        }

        if (project.getCategoryId() != null) {
            List<Integer> catSubs = subscriptionService.findSubscriberUserIds(
                    SubscriptionTypeEnum.CATEGORY, project.getCategoryId());
            for (int userId : catSubs) {
                notificationService.send(userId,
                        "Novo projeto na sua categoria",
                        "O projeto \"" + project.getTitle() + "\" foi publicado na sua categoria.",
                        NotificationTypeEnum.PROJECT_CREATED);
            }
        }

        List<Integer> councilorSubs = subscriptionService.findSubscriberUserIds(
                SubscriptionTypeEnum.COUNCILOR, project.getAuthorId());
        for (int userId : councilorSubs) {
            notificationService.send(userId,
                    "Novo projeto do vereador que você segue",
                    "O vereador publicou o projeto \"" + project.getTitle() + "\".",
                    NotificationTypeEnum.PROJECT_CREATED);
        }
    }

    @Override
    public void reject(int projectId, int moderatorId, String feedback) {
        if (projectId <= 0 || moderatorId <= 0) return;

        Project project = projectDao.findByid(projectId);
        if (project == null) return;

        ProjectStatusEnum previousStatus = project.getStatus();
        project.setModerationStatus(ModerationStatusEnum.REJECTED);
        project.setStatus(ProjectStatusEnum.REJECTED);
        projectDao.update(projectId, project);

        projectStatusHistoryService.recordStatusChange(
                projectId, previousStatus, ProjectStatusEnum.REJECTED, moderatorId, feedback);

        ProjectModeration moderation = new ProjectModeration();
        moderation.setProjectId(projectId);
        moderation.setModeratorId(moderatorId);
        moderation.setAction(ModerationStatusEnum.REJECTED);
        moderation.setFeedback(feedback);
        projectModerationDao.create(moderation);

        logger.log(Level.INFO, "Projeto rejeitado. ID: " + projectId);

        notificationService.send(project.getAuthorId(),
                "Projeto rejeitado",
                "Seu projeto \"" + project.getTitle() + "\" foi rejeitado. Motivo: " + feedback,
                NotificationTypeEnum.PROJECT_STATUS_CHANGED);
    }

    @Override
    public void updateStatus(int projectId, ProjectStatusEnum status, int moderatorId, String note) {
        if (projectId <= 0 || status == null) return;

        Project existing = projectDao.findByid(projectId);
        if (existing == null) return;

        projectStatusHistoryService.recordStatusChange(projectId, existing.getStatus(), status, moderatorId, note);
        existing.setStatus(status);
        projectDao.update(projectId, existing);

        notificationService.send(existing.getAuthorId(),
                "Status do projeto atualizado",
                "O status do projeto \"" + existing.getTitle() + "\" foi alterado para " + status.name() + ".",
                NotificationTypeEnum.PROJECT_STATUS_CHANGED);

        List<Integer> projectSubs = subscriptionService.findSubscriberUserIds(
                SubscriptionTypeEnum.PROJECT, projectId);
        for (int userId : projectSubs) {
            if (userId != existing.getAuthorId()) {
                notificationService.send(userId,
                        "Status do projeto atualizado",
                        "O projeto \"" + existing.getTitle() + "\" teve seu status alterado.",
                        NotificationTypeEnum.PROJECT_STATUS_CHANGED);
            }
        }
    }

    @Override
    public List<ProjectModeration> findByProjectId(int projectId) {
        if (projectId <= 0) return List.of();
        return projectModerationDao.findByProjectId(projectId);
    }
}
