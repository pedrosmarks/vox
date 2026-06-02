package br.com.fai.Vox.implementation.service.notification;

import br.com.fai.Vox.domain.Notification;
import br.com.fai.Vox.port.dao.notification.NotificationDao;
import br.com.fai.Vox.port.service.notification.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = Logger.getLogger(NotificationServiceImpl.class.getName());

    private final NotificationDao notificationDao;

    public NotificationServiceImpl(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    @Override
    public void send(int userId, String title, String message, Notification.NotificationType type) {
        if (userId <= 0 || title == null || title.isEmpty()) return;
        Notification entity = new Notification();
        entity.setUserId(userId);
        entity.setTitle(title);
        entity.setMessage(message);
        entity.setType(type);
        notificationDao.create(entity);
        logger.log(Level.INFO, "Notificação enviada para userId: " + userId);
    }

    @Override
    public List<Notification> findByUserId(int userId) {
        if (userId <= 0) return List.of();
        return notificationDao.findByUserId(userId);
    }

    @Override
    public List<Notification> findUnreadByUserId(int userId) {
        if (userId <= 0) return List.of();
        return notificationDao.findUnreadByUserId(userId);
    }

    @Override
    public int countUnread(int userId) {
        if (userId <= 0) return 0;
        return notificationDao.countUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(int notificationId) {
        if (notificationId <= 0) return;
        notificationDao.markAsRead(notificationId);
    }

    @Override
    public void markAllAsRead(int userId) {
        if (userId <= 0) return;
        notificationDao.markAllAsRead(userId);
    }
}
