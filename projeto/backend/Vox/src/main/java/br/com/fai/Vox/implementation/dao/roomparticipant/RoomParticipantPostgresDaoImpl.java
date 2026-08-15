package br.com.fai.Vox.implementation.dao.roomparticipant;

import br.com.fai.Vox.domain.RoomParticipant;
import br.com.fai.Vox.port.dao.roomparticipant.RoomParticipantDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomParticipantPostgresDaoImpl implements RoomParticipantDao {

    private static final Logger logger = Logger.getLogger(RoomParticipantPostgresDaoImpl.class.getName());

    private final Connection connection;

    public RoomParticipantPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int create(int roomId, int userId) {
        final String sql = "INSERT INTO room_participant (room_id, user_id, status, can_publish_audio, can_publish_video) " +
                "VALUES (?, ?, CAST(? AS participant_status), false, false)";
        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, roomId);
            ps.setInt(2, userId);
            ps.setString(3, RoomParticipant.ParticipantStatus.PENDING.name());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }

            connection.commit();
            rs.close();
            ps.close();

            logger.log(Level.INFO, "Solicitação de entrada criada. roomId=" + roomId + " userId=" + userId);
            return id;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao criar solicitação de entrada. Realizando rollback.");
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public RoomParticipant findByRoomAndUser(int roomId, int userId) {
        final String sql = "SELECT * FROM room_participant WHERE room_id = ? AND user_id = ? ORDER BY created_at DESC LIMIT 1";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, roomId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                RoomParticipant participant = mapResultSet(rs);
                rs.close();
                ps.close();
                return participant;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<RoomParticipant> findByRoomId(int roomId) {
        final List<RoomParticipant> participants = new ArrayList<>();
        final String sql = "SELECT * FROM room_participant WHERE room_id = ? ORDER BY requested_at ASC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                participants.add(mapResultSet(rs));
            }
            rs.close();
            ps.close();
            return participants;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateStatus(int id, RoomParticipant.ParticipantStatus status) {
        final String sql = "UPDATE room_participant SET status = CAST(? AS participant_status), " +
                "decided_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
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
    public void updatePermissions(int id, boolean canPublishAudio, boolean canPublishVideo) {
        final String sql = "UPDATE room_participant SET can_publish_audio = ?, can_publish_video = ?, " +
                "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setBoolean(1, canPublishAudio);
            ps.setBoolean(2, canPublishVideo);
            ps.setInt(3, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private RoomParticipant mapResultSet(ResultSet rs) throws SQLException {
        RoomParticipant participant = new RoomParticipant();
        participant.setId(rs.getInt("id"));
        participant.setRoomId(rs.getInt("room_id"));
        participant.setUserId(rs.getInt("user_id"));
        participant.setStatus(RoomParticipant.ParticipantStatus.valueOf(rs.getString("status").toUpperCase()));
        participant.setCanPublishAudio(rs.getBoolean("can_publish_audio"));
        participant.setCanPublishVideo(rs.getBoolean("can_publish_video"));

        Timestamp requestedAt = rs.getTimestamp("requested_at");
        if (requestedAt != null) participant.setRequestedAt(requestedAt.toLocalDateTime());

        Timestamp decidedAt = rs.getTimestamp("decided_at");
        if (decidedAt != null) participant.setDecidedAt(decidedAt.toLocalDateTime());

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) participant.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) participant.setUpdatedAt(updatedAt.toLocalDateTime());

        return participant;
    }
}
