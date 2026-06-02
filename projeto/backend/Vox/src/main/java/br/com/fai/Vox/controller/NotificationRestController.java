package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.Notification;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {

    private final NotificationService notificationService;
    private final AuthenticatedUserHelper authHelper;

    public NotificationRestController(NotificationService notificationService,
                                       AuthenticatedUserHelper authHelper) {
        this.notificationService = notificationService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> findAll(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        return ResponseEntity.ok(notificationService.findByUserId(userId));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> findUnread(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        return ResponseEntity.ok(notificationService.findUnreadByUserId(userId));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> countUnread(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        int count = notificationService.countUnread(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable final int id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
