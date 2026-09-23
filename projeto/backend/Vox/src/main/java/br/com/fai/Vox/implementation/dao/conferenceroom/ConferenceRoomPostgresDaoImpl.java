package br.com.fai.Vox.implementation.dao.conferenceroom;

import br.com.fai.Vox.domain.ConferenceRoom;
import br.com.fai.Vox.domain.dto.CreateConferenceRoomDto;
import br.com.fai.Vox.port.dao.conferenceroom.ConferenceRoomDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConferenceRoomPostgresDaoImpl implements ConferenceRoomDao {

    private static final Logger logger = Logger.getLogger(ConferenceRoomPostgresDaoImpl.class.getName());

    private final Connection connection;

    public ConferenceRoomPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int create(CreateConferenceRoomDto dto) {
        final String sql = "INSERT INTO conference_room (name, description, moderator_id, municipality_id, status) " +
                "VALUES (?, ?, ?, ?, CAST(? AS room_status))";
        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, dto.getName());
            ps.setString(2, dto.getDescription());
            ps.setInt(3, dto.getModeratorId());
            ps.setInt(4, dto.getMunicipalityId());
            ps.setString(5, ConferenceRoom.RoomStatus.OPEN.name());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }

            connection.commit();
            rs.close();
            ps.close();

            logger.log(Level.INFO, "Sala de conferência criada com sucesso. ID: " + id);
            return id;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao criar sala de conferência. Realizando rollback.");
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
        final String sql = "DELETE FROM conference_room WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.execute();
            ps.close();
            logger.log(Level.INFO, "Sala de conferência removida com sucesso. ID: " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConferenceRoom findById(int id) {
        final String sql = "SELECT * FROM conference_room WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ConferenceRoom room = mapResultSet(rs);
                rs.close();
                ps.close();
                return room;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<ConferenceRoom> findByMunicipalityId(int municipalityId) {
        final List<ConferenceRoom> rooms = new ArrayList<>();
        final String sql = "SELECT * FROM conference_room WHERE municipality_id = ? ORDER BY created_at DESC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, municipalityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rooms.add(mapResultSet(rs));
            }
            rs.close();
            ps.close();
            return rooms;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateStatus(int id, ConferenceRoom.RoomStatus status) {
        final String sql = "UPDATE conference_room SET status = CAST(? AS room_status), updated_at = CURRENT_TIMESTAMP WHERE id = ?";
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

    private ConferenceRoom mapResultSet(ResultSet rs) throws SQLException {
        ConferenceRoom room = new ConferenceRoom();
        room.setId(rs.getInt("id"));
        room.setName(rs.getString("name"));
        room.setDescription(rs.getString("description"));
        room.setModeratorId(rs.getInt("moderator_id"));
        room.setMunicipalityId(rs.getInt("municipality_id"));
        room.setStatus(ConferenceRoom.RoomStatus.valueOf(rs.getString("status").toUpperCase()));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) room.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) room.setUpdatedAt(updatedAt.toLocalDateTime());

        return room;
    }
}
