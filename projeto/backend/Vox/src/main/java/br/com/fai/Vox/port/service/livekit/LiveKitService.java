package br.com.fai.Vox.port.service.livekit;

import br.com.fai.Vox.domain.RoomParticipant;
import br.com.fai.Vox.domain.UserModel;

public interface LiveKitService {

    String generateToken(String roomName, UserModel user, RoomParticipant participant);

    void createRoom(String roomName);

    void removeParticipant(String roomName, String participantIdentity);

    void updateParticipantPermissions(String roomName, String participantIdentity,
                                      boolean canPublishAudio, boolean canPublishVideo);
}
