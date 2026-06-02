package br.com.fai.Vox.port.service.notification;

import br.com.fai.Vox.domain.Notification;

import java.util.List;

public interface NotificationService {
    void send(int userId, String title, String message, Notification.NotificationType type);
    List<Notification> findByUserId(int userId);
    List<Notification> findUnreadByUserId(int userId);
    int countUnread(int userId);
    void markAsRead(int notificationId);
    void markAllAsRead(int userId);
}
