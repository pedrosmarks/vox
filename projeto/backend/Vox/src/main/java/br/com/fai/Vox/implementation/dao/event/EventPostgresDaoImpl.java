package br.com.fai.Vox.implementation.dao.event;

import br.com.fai.Vox.domain.Event;
import br.com.fai.Vox.domain.dto.EventFilterDto;
import br.com.fai.Vox.port.dao.event.EventDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EventPostgresDaoImpl implements EventDao {

    private final Connection connection;

    public EventPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int create(Event e) {
        final String sql = "INSERT INTO event " +
                "(title, description, category_id, price, start_date, end_date, location, municipality_id, author_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"})) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getDescription());
            ps.setInt(3, e.getCategoryId());
            if (e.getPrice() != null) ps.setBigDecimal(4, e.getPrice()); else ps.setNull(4, Types.NUMERIC);
            setTimestamp(ps, 5, e.getStartDate());
            setTimestamp(ps, 6, e.getEndDate());
            ps.setString(7, e.getLocation());
            if (e.getMunicipalityId() != null) ps.setInt(8, e.getMunicipalityId()); else ps.setNull(8, Types.INTEGER);
            if (e.getAuthorId() != null) ps.setInt(9, e.getAuthorId()); else ps.setNull(9, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Event findById(int id) {
        final String sql = "SELECT * FROM event WHERE id = ?";
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
    public void update(int id, Event e) {
        // category_id preservado quando não informado (COALESCE).
        final String sql = "UPDATE event SET title = ?, description = ?, " +
                "category_id = COALESCE(?, category_id), price = ?, start_date = ?, end_date = ?, " +
                "location = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getDescription());
            if (e.getCategoryId() != null) ps.setInt(3, e.getCategoryId()); else ps.setNull(3, Types.INTEGER);
            if (e.getPrice() != null) ps.setBigDecimal(4, e.getPrice()); else ps.setNull(4, Types.NUMERIC);
            setTimestamp(ps, 5, e.getStartDate());
            setTimestamp(ps, 6, e.getEndDate());
            ps.setString(7, e.getLocation());
            ps.setInt(8, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(int id) {
        final String sql = "DELETE FROM event WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Event> find(EventFilterDto f) {
        StringBuilder sql = new StringBuilder("SELECT * FROM event WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, f);
        sql.append(" ORDER BY start_date ASC NULLS LAST, created_at DESC LIMIT ? OFFSET ?");

        int size = f.getSize() <= 0 ? 10 : Math.min(f.getSize(), 100);
        int page = Math.max(f.getPage(), 0);
        params.add(size);
        params.add(page * size);

        final List<Event> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return list;
    }

    @Override
    public long count(EventFilterDto f) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM event WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, f);
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /** Monta as cláusulas de filtro compartilhadas por find() e count(). */
    private void appendFilters(StringBuilder sql, List<Object> params, EventFilterDto f) {
        if (f.getCategoryId() != null) {
            sql.append(" AND category_id = ?");
            params.add(f.getCategoryId());
        }
        if (f.getMunicipalityId() != null) {
            sql.append(" AND municipality_id = ?");
            params.add(f.getMunicipalityId());
        }
        if (f.getSearch() != null && !f.getSearch().isBlank()) {
            sql.append(" AND (title ILIKE ? OR description ILIKE ?)");
            String like = "%" + f.getSearch().trim() + "%";
            params.add(like);
            params.add(like);
        }
        // free=true tem precedência sobre faixa de preço.
        if (Boolean.TRUE.equals(f.getFree())) {
            sql.append(" AND (price IS NULL OR price = 0)");
        } else {
            if (f.getMinPrice() != null) {
                sql.append(" AND price >= ?");
                params.add(f.getMinPrice());
            }
            if (f.getMaxPrice() != null) {
                sql.append(" AND price <= ?");
                params.add(f.getMaxPrice());
            }
        }
        if (Boolean.TRUE.equals(f.getHasImage())) {
            sql.append(" AND EXISTS (SELECT 1 FROM event_image ei WHERE ei.event_id = event.id)");
        }
        if (f.getStartFrom() != null) {
            sql.append(" AND start_date >= ?");
            params.add(Timestamp.valueOf(f.getStartFrom()));
        }
        if (f.getStartTo() != null) {
            sql.append(" AND start_date <= ?");
            params.add(Timestamp.valueOf(f.getStartTo()));
        }
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object p = params.get(i);
            if (p instanceof Integer) ps.setInt(i + 1, (Integer) p);
            else if (p instanceof Long) ps.setLong(i + 1, (Long) p);
            else if (p instanceof java.math.BigDecimal) ps.setBigDecimal(i + 1, (java.math.BigDecimal) p);
            else if (p instanceof Timestamp) ps.setTimestamp(i + 1, (Timestamp) p);
            else ps.setString(i + 1, (String) p);
        }
    }

    private void setTimestamp(PreparedStatement ps, int idx, java.time.LocalDateTime value) throws SQLException {
        if (value != null) ps.setTimestamp(idx, Timestamp.valueOf(value));
        else ps.setNull(idx, Types.TIMESTAMP);
    }

    private Event map(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("id"));
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setCategoryId(rs.getInt("category_id"));
        e.setPrice(rs.getBigDecimal("price"));
        Timestamp startDate = rs.getTimestamp("start_date");
        if (startDate != null) e.setStartDate(startDate.toLocalDateTime());
        Timestamp endDate = rs.getTimestamp("end_date");
        if (endDate != null) e.setEndDate(endDate.toLocalDateTime());
        e.setLocation(rs.getString("location"));
        int municipalityId = rs.getInt("municipality_id");
        if (!rs.wasNull()) e.setMunicipalityId(municipalityId);
        int authorId = rs.getInt("author_id");
        if (!rs.wasNull()) e.setAuthorId(authorId);
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) e.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) e.setUpdatedAt(updatedAt.toLocalDateTime());
        return e;
    }
}
