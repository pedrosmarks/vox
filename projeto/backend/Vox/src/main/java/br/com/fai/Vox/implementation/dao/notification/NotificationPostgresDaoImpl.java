package br.com.fai.Vox.implementation.dao.notification;

import br.com.fai.Vox.domain.Notification;
import br.com.fai.Vox.port.dao.notification.NotificationDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationPostgresDaoImpl implements NotificationDao {

    private static final Logger logger = Logger.getLogger(NotificationPostgresDaoImpl.class.getName());

    private final Connection connection;

    public NotificationPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(Notification entity) {
        final String sql = "INSERT INTO notification (user_id, title, message, type, read) " +
                "VALUES (?, ?, ?, CAST(? AS notification_type), false)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, entity.getUserId());
            ps.setString(2, entity.getTitle());
            ps.setString(3, entity.getMessage());
            ps.setString(4, entity.getType().name());
            ps.executeUpdate();
            ps.close();
            logger.log(Level.INFO, "Notificação criada para userId: " + entity.getUserId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Notification findByid(int id) {
        final String sql = "SELECT * FROM notification WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Notification entity = mapResultSetToNotification(rs);
                rs.close();
                ps.close();
                return entity;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Notification> findByUserId(int userId) {
        final List<Notification> list = new ArrayList<>();
        final String sql = "SELECT * FROM notification WHERE user_id = ? ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToNotification(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Notification> findUnreadByUserId(int userId) {
        final List<Notification> list = new ArrayList<>();
        final String sql = "SELECT * FROM notification WHERE user_id = ? AND read = false ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToNotification(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public int countUnreadByUserId(int userId) {
        final String sql = "SELECT COUNT(*) FROM notification WHERE user_id = ? AND read = false";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            if (rs.next()) count = rs.getInt(1);
            rs.close();
            ps.close();
            return count;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markAsRead(int id) {
        final String sql = "UPDATE notification SET read = true, read_at = CURRENT_TIMESTAMP WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markAllAsRead(int userId) {
        final String sql = "UPDATE notification SET read = true, read_at = CURRENT_TIMESTAMP WHERE user_id = ? AND read = false";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification entity = new Notification();
        entity.setId(rs.getInt("id"));
        entity.setUserId(rs.getInt("user_id"));
        entity.setTitle(rs.getString("title"));
        entity.setMessage(rs.getString("message"));
        entity.setType(Notification.NotificationType.valueOf(rs.getString("type").toUpperCase()));
        entity.setRead(rs.getBoolean("read"));
        Timestamp readAt = rs.getTimestamp("read_at");
        if (readAt != null) entity.setReadAt(readAt.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) entity.setCreatedAt(createdAt.toLocalDateTime());
        return entity;
    }
}
