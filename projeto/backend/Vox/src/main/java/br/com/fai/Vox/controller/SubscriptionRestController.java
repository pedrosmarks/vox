package br.com.fai.Vox.controller;
import br.com.fai.Vox.domain.enums.SubscriptionTypeEnum;

import br.com.fai.Vox.domain.Subscription;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.subscription.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionRestController {

    private final SubscriptionService subscriptionService;
    private final AuthenticatedUserHelper authHelper;

    public SubscriptionRestController(SubscriptionService subscriptionService,
                                       AuthenticatedUserHelper authHelper) {
        this.subscriptionService = subscriptionService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public ResponseEntity<List<Subscription>> findMy(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        return ResponseEntity.ok(subscriptionService.findByUserId(userId));
    }

    @PostMapping("/all-projects")
    public ResponseEntity<Void> subscribeAllProjects(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        subscriptionService.subscribe(userId, SubscriptionTypeEnum.ALL_PROJECTS, null);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/all-projects")
    public ResponseEntity<Void> unsubscribeAllProjects(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        subscriptionService.unsubscribe(userId, SubscriptionTypeEnum.ALL_PROJECTS, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/all-issues")
    public ResponseEntity<Void> subscribeAllIssues(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        subscriptionService.subscribe(userId, SubscriptionTypeEnum.ALL_ISSUES, null);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/all-issues")
    public ResponseEntity<Void> unsubscribeAllIssues(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        subscriptionService.unsubscribe(userId, SubscriptionTypeEnum.ALL_ISSUES, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects/{projectId}")
    public ResponseEntity<Void> subscribeProject(@PathVariable final int projectId,
                                                   HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        subscriptionService.subscribe(userId, SubscriptionTypeEnum.PROJECT, projectId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<Void> unsubscribeProject(@PathVariable final int projectId,
                                                     HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        subscriptionService.unsubscribe(userId, SubscriptionTypeEnum.PROJECT, projectId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/issues/{issueId}")
    public ResponseEntity<Void> subscribeIssue(@PathVariable final int issueId,
                                                HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        subscriptionService.subscribe(userId, SubscriptionTypeEnum.ISSUE, issueId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/issues/{issueId}")
    public ResponseEntity<Void> unsubscribeIssue(@PathVariable final int issueId,
                                                   HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        subscriptionService.unsubscribe(userId, SubscriptionTypeEnum.ISSUE, issueId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/categories/{categoryId}")
    public ResponseEntity<Void> subscribeCategory(@PathVariable final int categoryId,
                                                    HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        subscriptionService.subscribe(userId, SubscriptionTypeEnum.CATEGORY, categoryId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> unsubscribeCategory(@PathVariable final int categoryId,
                                                      HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        subscriptionService.unsubscribe(userId, SubscriptionTypeEnum.CATEGORY, categoryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/councilors/{councilorId}")
    public ResponseEntity<Void> subscribeCouncilor(@PathVariable final int councilorId,
                                                     HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        subscriptionService.subscribe(userId, SubscriptionTypeEnum.COUNCILOR, councilorId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/councilors/{councilorId}")
    public ResponseEntity<Void> unsubscribeCouncilor(@PathVariable final int councilorId,
                                                       HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        subscriptionService.unsubscribe(userId, SubscriptionTypeEnum.COUNCILOR, councilorId);
        return ResponseEntity.noContent().build();
    }
}
