package br.com.fai.Vox.implementation.dao.projectimage;

import br.com.fai.Vox.domain.ProjectImage;
import br.com.fai.Vox.port.dao.projectimage.ProjectImageDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectImagePostgresDaoImpl implements ProjectImageDao {

    private static final Logger logger = Logger.getLogger(ProjectImagePostgresDaoImpl.class.getName());

    private final Connection connection;

    public ProjectImagePostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int create(ProjectImage entity) {
        final String sql = "INSERT INTO project_image (project_id, url) VALUES (?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, entity.getProjectId());
            ps.setString(2, entity.getUrl());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }

            rs.close();
            ps.close();

            logger.log(Level.INFO, "Imagem criada com sucesso. ID: " + id);
            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        final String sql = "DELETE FROM project_image WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.execute();
            ps.close();
            logger.log(Level.INFO, "Imagem removida com sucesso. ID: " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProjectImage findByid(int id) {
        final String sql = "SELECT * FROM project_image WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ProjectImage image = mapResultSetToProjectImage(rs);
                rs.close();
                ps.close();
                return image;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<ProjectImage> findAll() {
        final List<ProjectImage> images = new ArrayList<>();
        final String sql = "SELECT * FROM project_image ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                images.add(mapResultSetToProjectImage(rs));
            }
            rs.close();
            ps.close();
            return images;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(int id, ProjectImage entity) {
        final String sql = "UPDATE project_image SET url = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, entity.getUrl());
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ProjectImage> findByProjectId(int projectId) {
        final List<ProjectImage> images = new ArrayList<>();
        final String sql = "SELECT * FROM project_image WHERE project_id = ? ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                images.add(mapResultSetToProjectImage(rs));
            }
            rs.close();
            ps.close();
            return images;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ProjectImage mapResultSetToProjectImage(ResultSet rs) throws SQLException {
        ProjectImage image = new ProjectImage();
        image.setId(rs.getInt("id"));
        image.setProjectId(rs.getInt("project_id"));
        image.setUrl(rs.getString("url"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) image.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) image.setUpdatedAt(updatedAt.toLocalDateTime());

        return image;
    }
}
