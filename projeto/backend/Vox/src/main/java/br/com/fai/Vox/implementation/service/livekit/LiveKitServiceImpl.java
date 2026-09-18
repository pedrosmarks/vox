package br.com.fai.Vox.implementation.service.livekit;

import br.com.fai.Vox.domain.RoomParticipant;
import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.port.service.livekit.LiveKitService;
import io.livekit.server.AccessToken;
import io.livekit.server.CanPublish;
import io.livekit.server.CanPublishData;
import io.livekit.server.CanPublishSources;
import io.livekit.server.CanSubscribe;
import io.livekit.server.RoomAdmin;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.RoomServiceClient;
import livekit.LivekitModels;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LiveKitServiceImpl implements LiveKitService {

    private static final Logger logger = Logger.getLogger(LiveKitServiceImpl.class.getName());

    private final String apiKey;
    private final String apiSecret;
    private final RoomServiceClient roomServiceClient;

    public LiveKitServiceImpl(String apiKey, String apiSecret, String livekitUrl) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.roomServiceClient = RoomServiceClient.createClient(livekitUrl, apiKey, apiSecret);
    }

    @Override
    public String generateToken(String roomName, UserModel user, RoomParticipant participant) {
        AccessToken token = new AccessToken(apiKey, apiSecret);
        token.setName(user.getName());
        token.setIdentity(String.valueOf(user.getId()));

        boolean isModerator = user.getRole() == UserModel.UserRole.MODERATOR
                || user.getRole() == UserModel.UserRole.ADMINISTRATOR;

        if (isModerator) {
            // Moderador: entra na sala, publica tudo, assina tudo, administra
            token.addGrants(
                    new RoomJoin(true),
                    new RoomName(roomName),
                    new CanPublish(true),
                    new CanSubscribe(true),
                    new CanPublishData(true),
                    new RoomAdmin(true)
            );
        } else {
            // Cidadão: entra na sala, assina tudo, publica somente o que o moderador liberou
            boolean canPublishAudio = Boolean.TRUE.equals(participant.getCanPublishAudio());
            boolean canPublishVideo = Boolean.TRUE.equals(participant.getCanPublishVideo());

            List<String> allowedSources = new ArrayList<>();
            if (canPublishAudio) {
                allowedSources.add("microphone");
            }
            if (canPublishVideo) {
                allowedSources.add("camera");
            }

            token.addGrants(
                    new RoomJoin(true),
                    new RoomName(roomName),
                    new CanSubscribe(true),
                    new CanPublish(!allowedSources.isEmpty()),
                    new CanPublishData(false),
                    new CanPublishSources(allowedSources)
            );
        }

        return token.toJwt();
    }

    @Override
    public void createRoom(String roomName) {
        try {
            Call<livekit.LivekitModels.Room> call = roomServiceClient.createRoom(roomName);
            Response<livekit.LivekitModels.Room> response = call.execute();

            if (!response.isSuccessful()) {
                logger.log(Level.WARNING, "Falha ao criar sala no LiveKit: HTTP "
                        + response.code() + " - " + response.message());
            } else {
                logger.log(Level.INFO, "Sala criada no LiveKit: " + roomName);
            }
        } catch (IOException e) {
            // Sala pode ser criada automaticamente pelo LiveKit na primeira conexão
            logger.log(Level.WARNING, "Erro de comunicação ao criar sala no LiveKit (não crítico): "
                    + e.getMessage());
        }
    }

    @Override
    public void removeParticipant(String roomName, String participantIdentity) {
        try {
            Call<Void> call = roomServiceClient.removeParticipant(roomName, participantIdentity);
            Response<Void> response = call.execute();

            if (!response.isSuccessful()) {
                logger.log(Level.WARNING, "Falha ao remover participante no LiveKit: HTTP "
                        + response.code());
            } else {
                logger.log(Level.INFO, "Participante removido no LiveKit. room=" + roomName
                        + " identity=" + participantIdentity);
            }
        } catch (IOException e) {
            // Participante pode não estar conectado — não é erro crítico
            logger.log(Level.INFO, "Participante não encontrado no LiveKit ao remover "
                    + "(pode não estar conectado): " + participantIdentity);
        }
    }

    @Override
    public void updateParticipantPermissions(String roomName, String participantIdentity,
                                             boolean canPublishAudio, boolean canPublishVideo) {
        try {
            boolean canPublish = canPublishAudio || canPublishVideo;

            LivekitModels.ParticipantPermission permission = LivekitModels.ParticipantPermission.newBuilder()
                    .setCanSubscribe(true)
                    .setCanPublish(canPublish)
                    .setCanPublishData(false)
                    .build();

            Call<LivekitModels.ParticipantInfo> call = roomServiceClient.updateParticipant(
                    roomName,
                    participantIdentity,
                    null,   // name — sem alteração
                    null,   // metadata — sem alteração
                    permission,
                    null    // attributes — sem alteração
            );
            Response<LivekitModels.ParticipantInfo> response = call.execute();

            if (!response.isSuccessful()) {
                logger.log(Level.WARNING, "Falha ao atualizar permissões no LiveKit: HTTP "
                        + response.code());
                throw new RuntimeException("Falha ao atualizar permissões no LiveKit: HTTP "
                        + response.code());
            }

            logger.log(Level.INFO, "Permissões atualizadas no LiveKit. room=" + roomName
                    + " identity=" + participantIdentity
                    + " canPublishAudio=" + canPublishAudio
                    + " canPublishVideo=" + canPublishVideo);

        } catch (IOException e) {
            // Participante pode não estar conectado — as permissões serão aplicadas via token na reconexão
            logger.log(Level.INFO, "Participante não conectado ao LiveKit durante atualização "
                    + "de permissões: " + participantIdentity);
        }
    }
}
