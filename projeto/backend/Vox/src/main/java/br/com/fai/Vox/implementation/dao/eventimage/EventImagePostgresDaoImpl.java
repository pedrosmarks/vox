package br.com.fai.Vox.implementation.dao.eventimage;

import br.com.fai.Vox.domain.EventImage;
import br.com.fai.Vox.port.dao.eventimage.EventImageDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EventImagePostgresDaoImpl implements EventImageDao {

    private final Connection connection;

    public EventImagePostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int create(EventImage e) {
        final String sql = "INSERT INTO event_image (event_id, url) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"})) {
            ps.setInt(1, e.getEventId());
            ps.setString(2, e.getUrl());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<EventImage> findByEventId(int eventId) {
        final List<EventImage> list = new ArrayList<>();
        final String sql = "SELECT * FROM event_image WHERE event_id = ? ORDER BY created_at ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return list;
    }

    @Override
    public EventImage findById(int id) {
        final String sql = "SELECT * FROM event_image WHERE id = ?";
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
    public void delete(int id) {
        final String sql = "DELETE FROM event_image WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private EventImage map(ResultSet rs) throws SQLException {
        EventImage e = new EventImage();
        e.setId(rs.getInt("id"));
        e.setEventId(rs.getInt("event_id"));
        e.setUrl(rs.getString("url"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) e.setCreatedAt(createdAt.toLocalDateTime());
        return e;
    }
}
