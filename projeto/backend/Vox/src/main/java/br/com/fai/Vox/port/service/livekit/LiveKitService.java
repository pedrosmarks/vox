package br.com.fai.Vox.port.service.livekit;

import br.com.fai.Vox.domain.RoomParticipant;
import br.com.fai.Vox.domain.UserModel;

public interface LiveKitService {

    /**
     * Gera um AccessToken LiveKit para o usuário entrar na sala.
     * As permissões são baseadas no papel do usuário.
     */
    String generateToken(String roomName, UserModel user, RoomParticipant participant);

    /**
     * Cria a sala no servidor LiveKit.
     */
    void createRoom(String roomName);

    /**
     * Remove o participante da sala no servidor LiveKit.
     */
    void removeParticipant(String roomName, String participantIdentity);

    /**
     * Atualiza as permissões de publicação do participante no servidor LiveKit.
     */
    void updateParticipantPermissions(String roomName, String participantIdentity,
                                      boolean canPublishAudio, boolean canPublishVideo);
}
