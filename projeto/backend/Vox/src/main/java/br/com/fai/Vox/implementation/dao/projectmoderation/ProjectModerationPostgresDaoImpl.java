package br.com.fai.Vox.implementation.dao.projectmoderation;

import br.com.fai.Vox.domain.ProjectModeration;
import br.com.fai.Vox.domain.enuns.ModerationStatus;
import br.com.fai.Vox.port.dao.projectmoderation.ProjectModerationDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectModerationPostgresDaoImpl implements ProjectModerationDao {

    private static final Logger logger = Logger.getLogger(ProjectModerationPostgresDaoImpl.class.getName());

    private final Connection connection;

    public ProjectModerationPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(ProjectModeration entity) {
        final String sql = "INSERT INTO project_moderation (project_id, moderator_id, action, feedback) " +
                "VALUES (?, ?, CAST(? AS moderation_action), ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, entity.getProjectId());
            ps.setInt(2, entity.getModeratorId());
            ps.setString(3, entity.getAction().name());
            ps.setString(4, entity.getFeedback());
            ps.executeUpdate();
            ps.close();
            logger.log(Level.INFO, "ProjectModeration registrada para project ID: " + entity.getProjectId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ProjectModeration> findByProjectId(int projectId) {
        final List<ProjectModeration> list = new ArrayList<>();
        final String sql = "SELECT * FROM project_moderation WHERE project_id = ? ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToProjectModeration(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private ProjectModeration mapResultSetToProjectModeration(ResultSet rs) throws SQLException {
        ProjectModeration entity = new ProjectModeration();
        entity.setId(rs.getInt("id"));
        entity.setProjectId(rs.getInt("project_id"));
        entity.setModeratorId(rs.getInt("moderator_id"));
        entity.setAction(ModerationStatus.valueOf(rs.getString("action").toUpperCase()));
        entity.setFeedback(rs.getString("feedback"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) entity.setCreatedAt(createdAt.toLocalDateTime());
        return entity;
    }
}
