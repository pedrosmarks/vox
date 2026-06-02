package br.com.fai.Vox.implementation.dao.issuestatushistory;

import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.IssueStatusHistory;
import br.com.fai.Vox.port.dao.issuestatushistory.IssueStatusHistoryDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IssueStatusHistoryPostgresDaoImpl implements IssueStatusHistoryDao {

    private static final Logger logger = Logger.getLogger(IssueStatusHistoryPostgresDaoImpl.class.getName());

    private final Connection connection;

    public IssueStatusHistoryPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(IssueStatusHistory entity) {
        final String sql = "INSERT INTO issue_status_history (issue_id, previous_status, new_status, changed_by, note) " +
                "VALUES (?, CAST(? AS issue_status), CAST(? AS issue_status), ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, entity.getIssueId());
            ps.setString(2, entity.getPreviousStatus() != null ? entity.getPreviousStatus().name() : null);
            ps.setString(3, entity.getNewStatus().name());
            ps.setInt(4, entity.getChangedBy());
            ps.setString(5, entity.getNote());
            ps.executeUpdate();
            ps.close();
            logger.log(Level.INFO, "IssueStatusHistory registrado para issue ID: " + entity.getIssueId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<IssueStatusHistory> findByIssueId(int issueId) {
        final List<IssueStatusHistory> list = new ArrayList<>();
        final String sql = "SELECT * FROM issue_status_history WHERE issue_id = ? ORDER BY created_at ASC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, issueId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToIssueStatusHistory(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private IssueStatusHistory mapResultSetToIssueStatusHistory(ResultSet rs) throws SQLException {
        IssueStatusHistory entity = new IssueStatusHistory();
        entity.setId(rs.getInt("id"));
        entity.setIssueId(rs.getInt("issue_id"));
        String prev = rs.getString("previous_status");
        if (prev != null) entity.setPreviousStatus(IssueReport.IssueStatus.valueOf(prev.toUpperCase()));
        entity.setNewStatus(IssueReport.IssueStatus.valueOf(rs.getString("new_status").toUpperCase()));
        entity.setChangedBy(rs.getInt("changed_by"));
        entity.setNote(rs.getString("note"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) entity.setCreatedAt(createdAt.toLocalDateTime());
        return entity;
    }
}
