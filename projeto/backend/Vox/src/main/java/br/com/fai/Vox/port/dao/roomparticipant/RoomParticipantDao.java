package br.com.fai.Vox.port.dao.roomparticipant;

import br.com.fai.Vox.domain.RoomParticipant;

import java.util.List;

public interface RoomParticipantDao {

    int create(int roomId, int userId);

    RoomParticipant findByRoomAndUser(int roomId, int userId);

    List<RoomParticipant> findByRoomId(int roomId);

    void updateStatus(int id, RoomParticipant.ParticipantStatus status);

    void updatePermissions(int id, boolean canPublishAudio, boolean canPublishVideo);
}
