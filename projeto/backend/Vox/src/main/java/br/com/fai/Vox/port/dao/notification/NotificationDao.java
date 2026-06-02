package br.com.fai.Vox.port.dao.notification;

import br.com.fai.Vox.domain.Notification;

import java.util.List;

public interface NotificationDao {
    void create(Notification entity);
    Notification findByid(int id);
    List<Notification> findByUserId(int userId);
    List<Notification> findUnreadByUserId(int userId);
    int countUnreadByUserId(int userId);
    void markAsRead(int id);
    void markAllAsRead(int userId);
}
