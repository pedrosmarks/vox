package br.com.fai.Vox.implementation.dao.issueimage;

import br.com.fai.Vox.domain.IssueImage;
import br.com.fai.Vox.port.dao.issueimage.IssueImageDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IssueImagePostgresDaoImpl implements IssueImageDao {

    private static final Logger logger = Logger.getLogger(IssueImagePostgresDaoImpl.class.getName());

    private final Connection connection;

    public IssueImagePostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int create(IssueImage entity) {
        final String sql = "INSERT INTO issue_image (issue_id, url) VALUES (?, ?)";
        try {
            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, entity.getIssueId());
            ps.setString(2, entity.getUrl());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int id = 0;
            if (rs.next()) id = rs.getInt(1);
            connection.commit();
            rs.close();
            ps.close();
            logger.log(Level.INFO, "IssueImage criada. ID: " + id);
            return id;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { throw new RuntimeException(ex); }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        final String sql = "DELETE FROM issue_image WHERE id = ?";
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
    public IssueImage findByid(int id) {
        final String sql = "SELECT * FROM issue_image WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                IssueImage entity = mapResultSetToIssueImage(rs);
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
    public List<IssueImage> findByIssueId(int issueId) {
        final List<IssueImage> list = new ArrayList<>();
        final String sql = "SELECT * FROM issue_image WHERE issue_id = ? ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, issueId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToIssueImage(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private IssueImage mapResultSetToIssueImage(ResultSet rs) throws SQLException {
        IssueImage entity = new IssueImage();
        entity.setId(rs.getInt("id"));
        entity.setIssueId(rs.getInt("issue_id"));
        entity.setUrl(rs.getString("url"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) entity.setCreatedAt(createdAt.toLocalDateTime());
        return entity;
    }
}
