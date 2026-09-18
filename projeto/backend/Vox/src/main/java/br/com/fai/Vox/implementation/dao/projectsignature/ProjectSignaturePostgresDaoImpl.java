package br.com.fai.Vox.implementation.dao.projectsignature;

import br.com.fai.Vox.domain.ProjectSignature;
import br.com.fai.Vox.port.dao.projectsignature.ProjectSignatureDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectSignaturePostgresDaoImpl implements ProjectSignatureDao {

    private static final Logger logger = Logger.getLogger(ProjectSignaturePostgresDaoImpl.class.getName());

    private final Connection connection;

    public ProjectSignaturePostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void sign(int projectId, int userId) {
        final String sql = "INSERT INTO project_signature (project_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try {
            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            ps.close();
            connection.commit();
            logger.log(Level.INFO, "Assinatura registrada. projectId=" + projectId + " userId=" + userId);
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
    public void unsign(int projectId, int userId) {
        final String sql = "DELETE FROM project_signature WHERE project_id = ? AND user_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            ps.close();
            logger.log(Level.INFO, "Assinatura removida. projectId=" + projectId + " userId=" + userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(int projectId, int userId) {
        final String sql = "SELECT 1 FROM project_signature WHERE project_id = ? AND user_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            boolean found = rs.next();
            rs.close();
            ps.close();
            return found;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long countByProjectId(int projectId) {
        final String sql = "SELECT COUNT(*) FROM project_signature WHERE project_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            long count = 0;
            if (rs.next()) count = rs.getLong(1);
            rs.close();
            ps.close();
            return count;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ProjectSignature> findByProjectId(int projectId) {
        final List<ProjectSignature> list = new ArrayList<>();
        final String sql = "SELECT * FROM project_signature WHERE project_id = ? ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
            rs.close();
            ps.close();
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProjectSignature findByProjectIdAndUserId(int projectId, int userId) {
        final String sql = "SELECT * FROM project_signature WHERE project_id = ? AND user_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ProjectSignature sig = mapResultSet(rs);
                rs.close();
                ps.close();
                return sig;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private ProjectSignature mapResultSet(ResultSet rs) throws SQLException {
        ProjectSignature sig = new ProjectSignature();
        sig.setId(rs.getInt("id"));
        sig.setProjectId(rs.getInt("project_id"));
        sig.setUserId(rs.getInt("user_id"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) sig.setCreatedAt(createdAt.toLocalDateTime());
        return sig;
    }
}
