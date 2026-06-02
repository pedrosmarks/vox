package br.com.fai.Vox.implementation.dao.subscription;

import br.com.fai.Vox.domain.Subscription;
import br.com.fai.Vox.port.dao.subscription.SubscriptionDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SubscriptionPostgresDaoImpl implements SubscriptionDao {

    private static final Logger logger = Logger.getLogger(SubscriptionPostgresDaoImpl.class.getName());

    private final Connection connection;

    public SubscriptionPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void subscribe(int userId, Subscription.SubscriptionType type, Integer targetId) {
        final String sql = buildInsertSql(type);
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, type.name());
            if (targetId != null) ps.setInt(3, targetId);
            ps.executeUpdate();
            ps.close();
            logger.log(Level.INFO, "Subscription criada: userId=" + userId + " type=" + type + " targetId=" + targetId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unsubscribe(int userId, Subscription.SubscriptionType type, Integer targetId) {
        final String sql = buildDeleteSql(type);
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, type.name());
            if (targetId != null) ps.setInt(3, targetId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Subscription> findByUserId(int userId) {
        final List<Subscription> list = new ArrayList<>();
        final String sql = "SELECT * FROM subscription WHERE user_id = ? ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToSubscription(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Subscription> findByTypeAndTargetId(Subscription.SubscriptionType type, Integer targetId) {
        final String column = resolveColumn(type);
        final String sql = column != null
                ? "SELECT * FROM subscription WHERE type = CAST(? AS subscription_type) AND " + column + " = ?"
                : "SELECT * FROM subscription WHERE type = CAST(? AS subscription_type)";
        final List<Subscription> list = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, type.name());
            if (column != null && targetId != null) ps.setInt(2, targetId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToSubscription(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Subscription> findByType(Subscription.SubscriptionType type) {
        final List<Subscription> list = new ArrayList<>();
        final String sql = "SELECT * FROM subscription WHERE type = CAST(? AS subscription_type)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToSubscription(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public boolean exists(int userId, Subscription.SubscriptionType type, Integer targetId) {
        final String column = resolveColumn(type);
        final String sql = column != null
                ? "SELECT 1 FROM subscription WHERE user_id = ? AND type = CAST(? AS subscription_type) AND " + column + " = ?"
                : "SELECT 1 FROM subscription WHERE user_id = ? AND type = CAST(? AS subscription_type)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, type.name());
            if (column != null && targetId != null) ps.setInt(3, targetId);
            ResultSet rs = ps.executeQuery();
            boolean exists = rs.next();
            rs.close();
            ps.close();
            return exists;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // --- Helpers ---

    private String resolveColumn(Subscription.SubscriptionType type) {
        return switch (type) {
            case PROJECT -> "project_id";
            case ISSUE -> "issue_id";
            case CATEGORY -> "category_id";
            case COUNCILOR -> "councilor_id";
            default -> null; // ALL_PROJECTS, ALL_ISSUES não têm coluna alvo
        };
    }

    private String buildInsertSql(Subscription.SubscriptionType type) {
        String column = resolveColumn(type);
        if (column == null) {
            return "INSERT INTO subscription (user_id, type) VALUES (?, CAST(? AS subscription_type)) ON CONFLICT DO NOTHING";
        }
        return "INSERT INTO subscription (user_id, type, " + column + ") VALUES (?, CAST(? AS subscription_type), ?) ON CONFLICT DO NOTHING";
    }

    private String buildDeleteSql(Subscription.SubscriptionType type) {
        String column = resolveColumn(type);
        if (column == null) {
            return "DELETE FROM subscription WHERE user_id = ? AND type = CAST(? AS subscription_type)";
        }
        return "DELETE FROM subscription WHERE user_id = ? AND type = CAST(? AS subscription_type) AND " + column + " = ?";
    }

    private Subscription mapResultSetToSubscription(ResultSet rs) throws SQLException {
        Subscription entity = new Subscription();
        entity.setId(rs.getInt("id"));
        entity.setUserId(rs.getInt("user_id"));
        entity.setType(Subscription.SubscriptionType.valueOf(rs.getString("type").toUpperCase()));

        int projectId = rs.getInt("project_id");
        if (!rs.wasNull()) entity.setProjectId(projectId);

        int issueId = rs.getInt("issue_id");
        if (!rs.wasNull()) entity.setIssueId(issueId);

        int categoryId = rs.getInt("category_id");
        if (!rs.wasNull()) entity.setCategoryId(categoryId);

        int councilorId = rs.getInt("councilor_id");
        if (!rs.wasNull()) entity.setCouncilorId(councilorId);

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) entity.setCreatedAt(createdAt.toLocalDateTime());
        return entity;
    }
}
