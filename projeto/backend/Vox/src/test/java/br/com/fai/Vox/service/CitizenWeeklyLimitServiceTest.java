package br.com.fai.Vox.service;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.dto.CreateProjectDto;
import br.com.fai.Vox.implementation.service.project.ProjectServiceImpl;
import br.com.fai.Vox.implementation.service.issuereport.IssueReportServiceImpl;
import br.com.fai.Vox.port.dao.issueimage.IssueImageDao;
import br.com.fai.Vox.port.dao.issuereport.IssueReportDao;
import br.com.fai.Vox.port.dao.project.ProjectDao;
import br.com.fai.Vox.port.dao.projectimage.ProjectImageDao;
import br.com.fai.Vox.port.service.drive.CloudinaryService;
import br.com.fai.Vox.port.service.issuereport.IssueReportService;
import br.com.fai.Vox.port.service.issuestatushistory.IssueStatusHistoryService;
import br.com.fai.Vox.port.service.notification.NotificationService;
import br.com.fai.Vox.port.service.project.ProjectService;
import br.com.fai.Vox.port.service.projectstatushistory.ProjectStatusHistoryService;
import br.com.fai.Vox.port.service.subscription.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitizenWeeklyLimitServiceTest {

    @Mock
    private ProjectDao projectDao;
    @Mock
    private ProjectImageDao projectImageDao;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private ProjectStatusHistoryService projectStatusHistoryService;

    @Mock
    private IssueReportDao issueReportDao;
    @Mock
    private IssueImageDao issueImageDao;
    @Mock
    private IssueStatusHistoryService issueStatusHistoryService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private SubscriptionService subscriptionService;

    @Test
    void projectService_rejectsCitizenWhenWeeklyLimitReached() {
        ProjectService service = new ProjectServiceImpl(projectDao, projectImageDao, cloudinaryService, projectStatusHistoryService);

        when(projectDao.countCreatedInLastWeek(10)).thenReturn(3L);

        assertThrows(IllegalArgumentException.class, () -> service.validateCitizenWeeklyCreateLimit(10));
    }

    @Test
    void projectService_allowsCitizenBelowWeeklyLimit() {
        ProjectService service = new ProjectServiceImpl(projectDao, projectImageDao, cloudinaryService, projectStatusHistoryService);

        when(projectDao.countCreatedInLastWeek(10)).thenReturn(2L);

        assertDoesNotThrow(() -> service.validateCitizenWeeklyCreateLimit(10));
    }

    @Test
    void issueReportService_rejectsCitizenWhenWeeklyLimitReached() {
        IssueReportService service = new IssueReportServiceImpl(
                issueReportDao,
                issueImageDao,
                cloudinaryService,
                issueStatusHistoryService,
                notificationService,
                subscriptionService
        );

        when(issueReportDao.countCreatedInLastWeek(20)).thenReturn(3L);

        assertThrows(IllegalArgumentException.class, () -> service.validateCitizenWeeklyCreateLimit(20));
    }

    @Test
    void issueReportService_allowsCitizenBelowWeeklyLimit() {
        IssueReportService service = new IssueReportServiceImpl(
                issueReportDao,
                issueImageDao,
                cloudinaryService,
                issueStatusHistoryService,
                notificationService,
                subscriptionService
        );

        when(issueReportDao.countCreatedInLastWeek(20)).thenReturn(1L);

        assertDoesNotThrow(() -> service.validateCitizenWeeklyCreateLimit(20));
    }
}
