package br.com.fai.Vox.implementation.dao.eventcategory;

import br.com.fai.Vox.domain.EventCategory;
import br.com.fai.Vox.port.dao.eventcategory.EventCategoryDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EventCategoryPostgresDaoImpl implements EventCategoryDao {

    private final Connection connection;

    public EventCategoryPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int create(EventCategory e) {
        final String sql = "INSERT INTO event_category (name, description) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"})) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public EventCategory findById(int id) {
        final String sql = "SELECT * FROM event_category WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<EventCategory> findAll() {
        final List<EventCategory> list = new ArrayList<>();
        final String sql = "SELECT * FROM event_category ORDER BY name ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return list;
    }

    @Override
    public void update(int id, EventCategory e) {
        final String sql = "UPDATE event_category SET name = ?, description = ?, " +
                "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getDescription());
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(int id) {
        final String sql = "DELETE FROM event_category WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private EventCategory map(ResultSet rs) throws SQLException {
        EventCategory e = new EventCategory();
        e.setId(rs.getInt("id"));
        e.setName(rs.getString("name"));
        e.setDescription(rs.getString("description"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) e.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) e.setUpdatedAt(updatedAt.toLocalDateTime());
        return e;
    }
}
