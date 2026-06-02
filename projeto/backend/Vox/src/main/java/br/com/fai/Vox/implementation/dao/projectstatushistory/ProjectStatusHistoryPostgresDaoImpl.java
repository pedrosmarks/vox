package br.com.fai.Vox.implementation.dao.projectstatushistory;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.ProjectStatusHistory;
import br.com.fai.Vox.port.dao.projectstatushistory.ProjectStatusHistoryDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectStatusHistoryPostgresDaoImpl implements ProjectStatusHistoryDao {

    private static final Logger logger = Logger.getLogger(ProjectStatusHistoryPostgresDaoImpl.class.getName());

    private final Connection connection;

    public ProjectStatusHistoryPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(ProjectStatusHistory entity) {
        final String sql = "INSERT INTO project_status_history (project_id, previous_status, new_status, changed_by, note) " +
                "VALUES (?, ?::project_status, ?::project_status, ?, ?)";
        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, entity.getProjectId());
            ps.setString(2, entity.getPreviousStatus() != null ? entity.getPreviousStatus().name() : null);
            ps.setString(3, entity.getNewStatus().name());
            ps.setInt(4, entity.getChangedBy());
            ps.setString(5, entity.getNote());

            ps.executeUpdate();
            ps.close();
            connection.commit();

            logger.log(Level.INFO, "Histórico de status criado para o projeto ID: " + entity.getProjectId());
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
    public List<ProjectStatusHistory> findByProjectId(int projectId) {
        final List<ProjectStatusHistory> history = new ArrayList<>();
        final String sql = "SELECT * FROM project_status_history WHERE project_id = ? ORDER BY created_at ASC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                history.add(mapResultSet(rs));
            }
            rs.close();
            ps.close();
            return history;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ProjectStatusHistory mapResultSet(ResultSet rs) throws SQLException {
        ProjectStatusHistory h = new ProjectStatusHistory();
        h.setId(rs.getInt("id"));
        h.setProjectId(rs.getInt("project_id"));
        h.setNewStatus(Project.ProjectStatus.valueOf(rs.getString("new_status")));
        h.setChangedBy(rs.getInt("changed_by"));
        h.setNote(rs.getString("note"));

        String prev = rs.getString("previous_status");
        if (prev != null) h.setPreviousStatus(Project.ProjectStatus.valueOf(prev));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) h.setCreatedAt(createdAt.toLocalDateTime());

        return h;
    }
}
