package br.com.fai.Vox.implementation.dao.projectopinion;

import br.com.fai.Vox.domain.ProjectOpinion;
import br.com.fai.Vox.port.dao.projectopinion.ProjectOpinionDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectOpinionPostgresDaoImpl implements ProjectOpinionDao {

    private static final Logger logger = Logger.getLogger(ProjectOpinionPostgresDaoImpl.class.getName());

    private final Connection connection;

    public ProjectOpinionPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void upsert(ProjectOpinion entity) {
        final String sql = "INSERT INTO project_opinion (project_id, user_id, opinion) " +
                "VALUES (?, ?, ?::vote_type) " +
                "ON CONFLICT (project_id, user_id) DO UPDATE SET opinion = EXCLUDED.opinion, updated_at = CURRENT_TIMESTAMP";
        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, entity.getProjectId());
            ps.setInt(2, entity.getUserId());
            ps.setString(3, entity.getOpinion().name());

            ps.executeUpdate();
            ps.close();
            connection.commit();

            logger.log(Level.INFO, "Opinião registrada para o projeto ID: " + entity.getProjectId());
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ProjectOpinion> findByProjectId(int projectId) {
        final List<ProjectOpinion> opinions = new ArrayList<>();
        final String sql = "SELECT * FROM project_opinion WHERE project_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                opinions.add(mapResultSet(rs));
            }
            rs.close();
            ps.close();
            return opinions;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProjectOpinion findByProjectIdAndUserId(int projectId, int userId) {
        final String sql = "SELECT * FROM project_opinion WHERE project_id = ? AND user_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ProjectOpinion opinion = mapResultSet(rs);
                rs.close();
                ps.close();
                return opinion;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private ProjectOpinion mapResultSet(ResultSet rs) throws SQLException {
        ProjectOpinion o = new ProjectOpinion();
        o.setId(rs.getInt("id"));
        o.setProjectId(rs.getInt("project_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setOpinion(ProjectOpinion.OpinionType.valueOf(rs.getString("opinion")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) o.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) o.setUpdatedAt(updatedAt.toLocalDateTime());

        return o;
    }
}
