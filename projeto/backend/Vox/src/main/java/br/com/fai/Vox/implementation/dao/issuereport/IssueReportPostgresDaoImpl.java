package br.com.fai.Vox.implementation.dao.issuereport;

import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.dto.CreateIssueReportDto;
import br.com.fai.Vox.domain.enuns.ModerationStatus;
import br.com.fai.Vox.port.dao.issuereport.IssueReportDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IssueReportPostgresDaoImpl implements IssueReportDao {

    private static final Logger logger = Logger.getLogger(IssueReportPostgresDaoImpl.class.getName());

    private final Connection connection;

    public IssueReportPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int create(CreateIssueReportDto dto) {
        final String sql = "INSERT INTO issue_report " +
                "(municipality_id, author_id, councilor_id, title, description, neighborhood, street, number, " +
                "latitude, longitude, status, moderation_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS issue_status), CAST(? AS moderation_status))";
        try {
            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, dto.getMunicipalityId());
            ps.setInt(2, dto.getAuthorId());
            if (dto.getCouncilorId() != null) ps.setInt(3, dto.getCouncilorId());
            else ps.setNull(3, Types.INTEGER);
            ps.setString(4, dto.getTitle());
            ps.setString(5, dto.getDescription());
            ps.setString(6, dto.getNeighborhood());
            ps.setString(7, dto.getStreet());
            ps.setString(8, dto.getNumber());
            ps.setBigDecimal(9, dto.getLatitude());
            ps.setBigDecimal(10, dto.getLongitude());
            ps.setString(11, IssueReport.IssueStatus.OPEN.name());
            ps.setString(12, ModerationStatus.PENDING.name());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int id = 0;
            if (rs.next()) id = rs.getInt(1);
            connection.commit();
            rs.close();
            ps.close();
            logger.log(Level.INFO, "IssueReport criado. ID: " + id);
            return id;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao criar issue_report. Rollback.");
            try { connection.rollback(); } catch (SQLException ex) { throw new RuntimeException(ex); }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        final String sql = "DELETE FROM issue_report WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IssueReport findByid(int id) {
        final String sql = "SELECT * FROM issue_report WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                IssueReport entity = mapResultSetToIssueReport(rs);
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
    public List<IssueReport> findAll() {
        final List<IssueReport> list = new ArrayList<>();
        final String sql = "SELECT * FROM issue_report ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToIssueReport(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<IssueReport> findByMunicipalityId(int municipalityId) {
        final List<IssueReport> list = new ArrayList<>();
        final String sql = "SELECT * FROM issue_report WHERE municipality_id = ? ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, municipalityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToIssueReport(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<IssueReport> findByAuthorId(int authorId) {
        final List<IssueReport> list = new ArrayList<>();
        final String sql = "SELECT * FROM issue_report WHERE author_id = ? ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, authorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToIssueReport(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<IssueReport> findByMunicipalityIdAndModerationStatus(int municipalityId, ModerationStatus moderationStatus) {
        final List<IssueReport> list = new ArrayList<>();
        final String sql = "SELECT * FROM issue_report WHERE municipality_id = ? AND moderation_status = CAST(? AS moderation_status) ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, municipalityId);
            ps.setString(2, moderationStatus.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToIssueReport(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public void update(int id, IssueReport entity) {
        final String sql = "UPDATE issue_report SET councilor_id = ?, title = ?, description = ?, " +
                "neighborhood = ?, street = ?, number = ?, latitude = ?, longitude = ?, " +
                "status = CAST(? AS issue_status), updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try {
            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement(sql);
            if (entity.getCouncilorId() != null) ps.setInt(1, entity.getCouncilorId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, entity.getTitle());
            ps.setString(3, entity.getDescription());
            ps.setString(4, entity.getNeighborhood());
            ps.setString(5, entity.getStreet());
            ps.setString(6, entity.getNumber());
            ps.setBigDecimal(7, entity.getLatitude());
            ps.setBigDecimal(8, entity.getLongitude());
            ps.setString(9, entity.getStatus().name());
            ps.setInt(10, id);
            ps.executeUpdate();
            ps.close();
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { throw new RuntimeException(ex); }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateModerationStatus(int id, ModerationStatus status) {
        final String sql = "UPDATE issue_report SET moderation_status = CAST(? AS moderation_status), updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateStatus(int id, IssueReport.IssueStatus status) {
        final String sql = "UPDATE issue_report SET status = CAST(? AS issue_status), updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private IssueReport mapResultSetToIssueReport(ResultSet rs) throws SQLException {
        IssueReport entity = new IssueReport();
        entity.setId(rs.getInt("id"));
        entity.setMunicipalityId(rs.getInt("municipality_id"));
        entity.setAuthorId(rs.getInt("author_id"));
        int councilorId = rs.getInt("councilor_id");
        if (!rs.wasNull()) entity.setCouncilorId(councilorId);
        entity.setTitle(rs.getString("title"));
        entity.setDescription(rs.getString("description"));
        entity.setNeighborhood(rs.getString("neighborhood"));
        entity.setStreet(rs.getString("street"));
        entity.setNumber(rs.getString("number"));
        entity.setLatitude(rs.getBigDecimal("latitude"));
        entity.setLongitude(rs.getBigDecimal("longitude"));
        entity.setStatus(IssueReport.IssueStatus.valueOf(rs.getString("status").toUpperCase()));
        entity.setModerationStatus(ModerationStatus.valueOf(rs.getString("moderation_status").toUpperCase()));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) entity.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) entity.setUpdatedAt(updatedAt.toLocalDateTime());
        return entity;
    }
}
