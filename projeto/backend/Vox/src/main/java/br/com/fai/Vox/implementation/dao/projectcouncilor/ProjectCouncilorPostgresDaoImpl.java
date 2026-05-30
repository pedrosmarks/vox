package br.com.fai.Vox.implementation.dao.projectcouncilor;

import br.com.fai.Vox.domain.ProjectCouncilor;
import br.com.fai.Vox.port.dao.projectcouncilor.ProjectCouncilorDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectCouncilorPostgresDaoImpl implements ProjectCouncilorDao {

    private static final Logger logger = Logger.getLogger(ProjectCouncilorPostgresDaoImpl.class.getName());

    private final Connection connection;

    public ProjectCouncilorPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void add(int projectId, int councilorId) {
        final String sql = "INSERT INTO project_councilor (project_id, councilor_id) VALUES (?, ?)";
        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, councilorId);
            ps.executeUpdate();
            ps.close();
            connection.commit();

            logger.log(Level.INFO, "Vereador ID " + councilorId + " adicionado ao projeto ID: " + projectId);
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
    public void remove(int projectId, int councilorId) {
        final String sql = "DELETE FROM project_councilor WHERE project_id = ? AND councilor_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, councilorId);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ProjectCouncilor> findByProjectId(int projectId) {
        final List<ProjectCouncilor> list = new ArrayList<>();
        final String sql = "SELECT * FROM project_councilor WHERE project_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            rs.close();
            ps.close();
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ProjectCouncilor mapResultSet(ResultSet rs) throws SQLException {
        ProjectCouncilor pc = new ProjectCouncilor();
        pc.setId(rs.getInt("id"));
        pc.setProjectId(rs.getInt("project_id"));
        pc.setCouncilorId(rs.getInt("councilor_id"));
        return pc;
    }
}
