package br.com.fai.Vox.implementation.dao.issuemoderation;

import br.com.fai.Vox.domain.IssueModeration;
import br.com.fai.Vox.domain.enuns.ModerationStatus;
import br.com.fai.Vox.port.dao.issuemoderation.IssueModerationDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IssueModerationPostgresDaoImpl implements IssueModerationDao {

    private static final Logger logger = Logger.getLogger(IssueModerationPostgresDaoImpl.class.getName());

    private final Connection connection;

    public IssueModerationPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(IssueModeration entity) {
        final String sql = "INSERT INTO issue_moderation (issue_id, moderator_id, action, feedback) " +
                "VALUES (?, ?, CAST(? AS moderation_status), ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, entity.getIssueId());
            ps.setInt(2, entity.getModeratorId());
            ps.setString(3, entity.getAction().name());
            ps.setString(4, entity.getFeedback());
            ps.executeUpdate();
            ps.close();
            logger.log(Level.INFO, "IssueModeration registrada para issue ID: " + entity.getIssueId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<IssueModeration> findByIssueId(int issueId) {
        final List<IssueModeration> list = new ArrayList<>();
        final String sql = "SELECT * FROM issue_moderation WHERE issue_id = ? ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, issueId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToIssueModeration(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private IssueModeration mapResultSetToIssueModeration(ResultSet rs) throws SQLException {
        IssueModeration entity = new IssueModeration();
        entity.setId(rs.getInt("id"));
        entity.setIssueId(rs.getInt("issue_id"));
        entity.setModeratorId(rs.getInt("moderator_id"));
        entity.setAction(ModerationStatus.valueOf(rs.getString("action").toUpperCase()));
        entity.setFeedback(rs.getString("feedback"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) entity.setCreatedAt(createdAt.toLocalDateTime());
        return entity;
    }
}
