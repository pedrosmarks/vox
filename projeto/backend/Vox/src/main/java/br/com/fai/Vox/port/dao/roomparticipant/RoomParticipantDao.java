package br.com.fai.Vox.port.dao.roomparticipant;
import br.com.fai.Vox.domain.enums.ParticipantStatusEnum;
import br.com.fai.Vox.domain.enums.SpeechRequestStatusEnum;

import br.com.fai.Vox.domain.RoomParticipant;

import java.util.List;

public interface RoomParticipantDao {

    int create(int roomId, int userId);

    RoomParticipant findByRoomAndUser(int roomId, int userId);

    List<RoomParticipant> findByRoomId(int roomId);

    void updateStatus(int id, ParticipantStatusEnum status);

    List<RoomParticipant> findPendingSpeechRequests(int roomId);

    void updateSpeechRequestStatus(int id, SpeechRequestStatusEnum status);

    void updatePermissions(int id, boolean canPublishAudio, boolean canPublishVideo);
}
