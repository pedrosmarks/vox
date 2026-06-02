package br.com.fai.Vox.implementation.dao.project;

import br.com.fai.Vox.domain.Project;
import br.com.fai.Vox.domain.dto.CreateProjectDto;
import br.com.fai.Vox.domain.enuns.ModerationStatus;
import br.com.fai.Vox.port.dao.project.ProjectDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectPostgresDaoImpl implements ProjectDao {

    private static final Logger logger = Logger.getLogger(ProjectPostgresDaoImpl.class.getName());

    private final Connection connection;

    public ProjectPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int create(CreateProjectDto dto) {
        final String sql = "INSERT INTO project " +
                "(municipality_id, category_id, type, title, description, status, moderation_status, author_id, highlighted, is_official, " +
                "neighborhood, street, number, latitude, longitude, start_date, expected_end_date, end_date, " +
                "financial_analysis, estimated_cost, approved_budget) " +
                "VALUES (?, ?, CAST(? AS project_type), ?, ?, CAST(? AS project_status), CAST(? AS moderation_status), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, dto.getMunicipalityId());
            ps.setInt(2, dto.getCategoryId());
            ps.setString(3, dto.getType().name());
            ps.setString(4, dto.getTitle());
            ps.setString(5, dto.getDescription());
            ps.setString(6, dto.getStatus() != null ? dto.getStatus().name() : "PENDING_APPROVAL");
            ps.setString(7, ModerationStatus.PENDING.name());
            ps.setInt(8, dto.getAuthorId());
            ps.setBoolean(9, dto.getHighlighted() != null && dto.getHighlighted());
            ps.setBoolean(10, dto.getIsOfficial() != null && dto.getIsOfficial());
            ps.setString(11, dto.getNeighborhood());
            ps.setString(12, dto.getStreet());
            ps.setString(13, dto.getNumber());
            ps.setBigDecimal(14, dto.getLatitude());
            ps.setBigDecimal(15, dto.getLongitude());
            ps.setObject(16, dto.getStartDate());
            ps.setObject(17, dto.getExpectedEndDate());
            ps.setObject(18, dto.getEndDate());
            ps.setString(19, dto.getFinancialAnalysis());
            ps.setBigDecimal(20, dto.getEstimatedCost());
            ps.setBigDecimal(21, dto.getApprovedBudget());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }

            connection.commit();
            rs.close();
            ps.close();

            logger.log(Level.INFO, "Projeto criado com sucesso. ID: " + id);
            return id;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao criar projeto. Realizando rollback.");
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        final String sql = "DELETE FROM project WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.execute();
            ps.close();
            logger.log(Level.INFO, "Projeto removido com sucesso. ID: " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Project findByid(int id) {
        final String sql = "SELECT * FROM project WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Project project = mapResultSetToProject(rs);
                rs.close();
                ps.close();
                return project;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Project> findAll() {
        final List<Project> projects = new ArrayList<>();
        final String sql = "SELECT * FROM project ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
            rs.close();
            ps.close();
            return projects;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(int id, Project entity) {
        final String sql = "UPDATE project SET " +
                "municipality_id = ?, category_id = ?, type = CAST(? AS project_type), title = ?, description = ?, " +
                "status = CAST(? AS project_status), moderation_status = CAST(? AS moderation_status), highlighted = ?, is_official = ?, " +
                "neighborhood = ?, street = ?, number = ?, latitude = ?, longitude = ?, " +
                "start_date = ?, expected_end_date = ?, end_date = ?, " +
                "financial_analysis = ?, estimated_cost = ?, approved_budget = ?, " +
                "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, entity.getMunicipalityId());
            ps.setInt(2, entity.getCategoryId());
            ps.setString(3, entity.getType().name());
            ps.setString(4, entity.getTitle());
            ps.setString(5, entity.getDescription());
            ps.setString(6, entity.getStatus().name());
            ps.setString(7, entity.getModerationStatus() != null ? entity.getModerationStatus().name() : ModerationStatus.PENDING.name());
            ps.setBoolean(8, entity.getHighlighted() != null && entity.getHighlighted());
            ps.setBoolean(9, entity.getIsOfficial() != null && entity.getIsOfficial());
            ps.setString(10, entity.getNeighborhood());
            ps.setString(11, entity.getStreet());
            ps.setString(12, entity.getNumber());
            ps.setBigDecimal(13, entity.getLatitude());
            ps.setBigDecimal(14, entity.getLongitude());
            ps.setObject(15, entity.getStartDate());
            ps.setObject(16, entity.getExpectedEndDate());
            ps.setObject(17, entity.getEndDate());
            ps.setString(18, entity.getFinancialAnalysis());
            ps.setBigDecimal(19, entity.getEstimatedCost());
            ps.setBigDecimal(20, entity.getApprovedBudget());
            ps.setInt(21, id);

            ps.executeUpdate();
            ps.close();
            connection.commit();
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
    public List<Project> findByMunicipalityId(int municipalityId) {
        final List<Project> projects = new ArrayList<>();
        final String sql = "SELECT * FROM project WHERE municipality_id = ? ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, municipalityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
            rs.close();
            ps.close();
            return projects;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Project> findByAuthorId(int authorId) {
        final List<Project> projects = new ArrayList<>();
        final String sql = "SELECT * FROM project WHERE author_id = ? ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, authorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
            rs.close();
            ps.close();
            return projects;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getInt("id"));
        project.setMunicipalityId(rs.getInt("municipality_id"));
        project.setCategoryId(rs.getInt("category_id"));
        project.setType(Project.ProjectType.valueOf(rs.getString("type").toUpperCase()));
        project.setTitle(rs.getString("title"));
        project.setDescription(rs.getString("description"));
        project.setStatus(Project.ProjectStatus.valueOf(rs.getString("status").toUpperCase()));
        String modStatus = rs.getString("moderation_status");
        if (modStatus != null) project.setModerationStatus(ModerationStatus.valueOf(modStatus.toUpperCase()));
        project.setAuthorId(rs.getInt("author_id"));
        project.setHighlighted(rs.getBoolean("highlighted"));
        project.setIsOfficial(rs.getBoolean("is_official"));
        project.setNeighborhood(rs.getString("neighborhood"));
        project.setStreet(rs.getString("street"));
        project.setNumber(rs.getString("number"));
        project.setLatitude(rs.getBigDecimal("latitude"));
        project.setLongitude(rs.getBigDecimal("longitude"));
        project.setFinancialAnalysis(rs.getString("financial_analysis"));
        project.setEstimatedCost(rs.getBigDecimal("estimated_cost"));
        project.setApprovedBudget(rs.getBigDecimal("approved_budget"));

        Date startDate = rs.getDate("start_date");
        if (startDate != null) project.setStartDate(startDate.toLocalDate());

        Date expectedEndDate = rs.getDate("expected_end_date");
        if (expectedEndDate != null) project.setExpectedEndDate(expectedEndDate.toLocalDate());

        Date endDate = rs.getDate("end_date");
        if (endDate != null) project.setEndDate(endDate.toLocalDate());

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) project.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) project.setUpdatedAt(updatedAt.toLocalDateTime());

        return project;
    }
}
