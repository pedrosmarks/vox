package br.com.fai.Vox.implementation.service.conferenceroom;

import br.com.fai.Vox.domain.ConferenceRoom;
import br.com.fai.Vox.domain.RoomParticipant;
import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.CreateConferenceRoomDto;
import br.com.fai.Vox.port.dao.conferenceroom.ConferenceRoomDao;
import br.com.fai.Vox.port.dao.roomparticipant.RoomParticipantDao;
import br.com.fai.Vox.port.service.conferenceroom.ConferenceRoomService;
import br.com.fai.Vox.port.service.livekit.LiveKitService;
import br.com.fai.Vox.port.service.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ConferenceRoomServiceImpl implements ConferenceRoomService {

    private static final Logger logger = Logger.getLogger(ConferenceRoomServiceImpl.class.getName());

    private final ConferenceRoomDao conferenceRoomDao;
    private final RoomParticipantDao roomParticipantDao;
    private final LiveKitService liveKitService;
    private final UserService userService;

    public ConferenceRoomServiceImpl(ConferenceRoomDao conferenceRoomDao,
                                     RoomParticipantDao roomParticipantDao,
                                     LiveKitService liveKitService,
                                     UserService userService) {
        this.conferenceRoomDao = conferenceRoomDao;
        this.roomParticipantDao = roomParticipantDao;
        this.liveKitService = liveKitService;
        this.userService = userService;
    }

    @Override
    public int create(CreateConferenceRoomDto dto, int moderatorId, int municipalityId) {
        if (dto == null || dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da sala é obrigatório");
        }

        dto.setModeratorId(moderatorId);
        dto.setMunicipalityId(municipalityId);

        int roomId = conferenceRoomDao.create(dto);
        String roomName = buildRoomName(roomId);

        liveKitService.createRoom(roomName);

        logger.log(Level.INFO, "Sala de conferência criada. ID: " + roomId + " LiveKit room: " + roomName);
        return roomId;
    }

    @Override
    public void delete(int roomId, int requestingUserId) {
        ConferenceRoom room = getExistingRoom(roomId);
        requireModerator(room, requestingUserId);

        conferenceRoomDao.updateStatus(roomId, ConferenceRoom.RoomStatus.CLOSED);
        logger.log(Level.INFO, "Sala de conferência encerrada. ID: " + roomId);
    }

    @Override
    public ConferenceRoom findById(int roomId) {
        ConferenceRoom room = conferenceRoomDao.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Sala não encontrada: " + roomId);
        }
        return room;
    }

    @Override
    public List<ConferenceRoom> findByMunicipalityId(int municipalityId) {
        return conferenceRoomDao.findByMunicipalityId(municipalityId);
    }

    @Override
    public void requestEntry(int roomId, int userId) {
        ConferenceRoom room = getExistingRoom(roomId);

        if (room.getStatus() == ConferenceRoom.RoomStatus.CLOSED) {
            throw new IllegalArgumentException("A sala está encerrada");
        }

        RoomParticipant existing = roomParticipantDao.findByRoomAndUser(roomId, userId);
        if (existing != null) {
            RoomParticipant.ParticipantStatus status = existing.getStatus();
            if (status == RoomParticipant.ParticipantStatus.PENDING) {
                throw new IllegalArgumentException("Já existe uma solicitação pendente para esta sala");
            }
            if (status == RoomParticipant.ParticipantStatus.APPROVED) {
                throw new IllegalArgumentException("Você já está aprovado nesta sala");
            }
        }

        roomParticipantDao.create(roomId, userId);
        logger.log(Level.INFO, "Solicitação de entrada criada. roomId=" + roomId + " userId=" + userId);
    }

    @Override
    public List<RoomParticipant> findRequests(int roomId) {
        getExistingRoom(roomId);
        return roomParticipantDao.findByRoomId(roomId);
    }

    @Override
    public void approveEntry(int roomId, int participantId, int moderatorId) {
        ConferenceRoom room = getExistingRoom(roomId);
        requireModerator(room, moderatorId);

        RoomParticipant participant = getParticipantInRoom(roomId, participantId);

        if (participant.getStatus() != RoomParticipant.ParticipantStatus.PENDING) {
            throw new IllegalArgumentException("A solicitação não está pendente");
        }

        roomParticipantDao.updateStatus(participant.getId(), RoomParticipant.ParticipantStatus.APPROVED);
        logger.log(Level.INFO, "Participante aprovado. roomId=" + roomId + " userId=" + participantId);
    }

    @Override
    public void rejectEntry(int roomId, int participantId, int moderatorId) {
        ConferenceRoom room = getExistingRoom(roomId);
        requireModerator(room, moderatorId);

        RoomParticipant participant = getParticipantInRoom(roomId, participantId);

        if (participant.getStatus() != RoomParticipant.ParticipantStatus.PENDING) {
            throw new IllegalArgumentException("A solicitação não está pendente");
        }

        roomParticipantDao.updateStatus(participant.getId(), RoomParticipant.ParticipantStatus.REJECTED);
        logger.log(Level.INFO, "Participante rejeitado. roomId=" + roomId + " userId=" + participantId);
    }

    @Override
    public void enableMicrophone(int roomId, int participantId, int moderatorId) {
        ConferenceRoom room = getExistingRoom(roomId);
        requireModerator(room, moderatorId);

        RoomParticipant participant = getApprovedParticipantInRoom(roomId, participantId);
        boolean newCanPublishVideo = Boolean.TRUE.equals(participant.getCanPublishVideo());

        roomParticipantDao.updatePermissions(participant.getId(), true, newCanPublishVideo);

        try {
            liveKitService.updateParticipantPermissions(
                    buildRoomName(roomId),
                    String.valueOf(participantId),
                    true,
                    newCanPublishVideo
            );
        } catch (RuntimeException e) {
            // Rollback do estado no banco
            roomParticipantDao.updatePermissions(participant.getId(),
                    Boolean.TRUE.equals(participant.getCanPublishAudio()), newCanPublishVideo);
            throw new RuntimeException("Falha ao liberar microfone no LiveKit: " + e.getMessage());
        }

        logger.log(Level.INFO, "Microfone liberado. roomId=" + roomId + " userId=" + participantId);
    }

    @Override
    public void disableMicrophone(int roomId, int participantId, int moderatorId) {
        ConferenceRoom room = getExistingRoom(roomId);
        requireModerator(room, moderatorId);

        RoomParticipant participant = getApprovedParticipantInRoom(roomId, participantId);
        boolean currentCanPublishVideo = Boolean.TRUE.equals(participant.getCanPublishVideo());

        roomParticipantDao.updatePermissions(participant.getId(), false, currentCanPublishVideo);

        try {
            liveKitService.updateParticipantPermissions(
                    buildRoomName(roomId),
                    String.valueOf(participantId),
                    false,
                    currentCanPublishVideo
            );
        } catch (RuntimeException e) {
            roomParticipantDao.updatePermissions(participant.getId(),
                    Boolean.TRUE.equals(participant.getCanPublishAudio()), currentCanPublishVideo);
            throw new RuntimeException("Falha ao bloquear microfone no LiveKit: " + e.getMessage());
        }

        logger.log(Level.INFO, "Microfone bloqueado. roomId=" + roomId + " userId=" + participantId);
    }

    @Override
    public void enableCamera(int roomId, int participantId, int moderatorId) {
        ConferenceRoom room = getExistingRoom(roomId);
        requireModerator(room, moderatorId);

        RoomParticipant participant = getApprovedParticipantInRoom(roomId, participantId);
        boolean currentCanPublishAudio = Boolean.TRUE.equals(participant.getCanPublishAudio());

        roomParticipantDao.updatePermissions(participant.getId(), currentCanPublishAudio, true);

        try {
            liveKitService.updateParticipantPermissions(
                    buildRoomName(roomId),
                    String.valueOf(participantId),
                    currentCanPublishAudio,
                    true
            );
        } catch (RuntimeException e) {
            roomParticipantDao.updatePermissions(participant.getId(),
                    currentCanPublishAudio, Boolean.TRUE.equals(participant.getCanPublishVideo()));
            throw new RuntimeException("Falha ao liberar câmera no LiveKit: " + e.getMessage());
        }

        logger.log(Level.INFO, "Câmera liberada. roomId=" + roomId + " userId=" + participantId);
    }

    @Override
    public void disableCamera(int roomId, int participantId, int moderatorId) {
        ConferenceRoom room = getExistingRoom(roomId);
        requireModerator(room, moderatorId);

        RoomParticipant participant = getApprovedParticipantInRoom(roomId, participantId);
        boolean currentCanPublishAudio = Boolean.TRUE.equals(participant.getCanPublishAudio());

        roomParticipantDao.updatePermissions(participant.getId(), currentCanPublishAudio, false);

        try {
            liveKitService.updateParticipantPermissions(
                    buildRoomName(roomId),
                    String.valueOf(participantId),
                    currentCanPublishAudio,
                    false
            );
        } catch (RuntimeException e) {
            roomParticipantDao.updatePermissions(participant.getId(),
                    currentCanPublishAudio, Boolean.TRUE.equals(participant.getCanPublishVideo()));
            throw new RuntimeException("Falha ao bloquear câmera no LiveKit: " + e.getMessage());
        }

        logger.log(Level.INFO, "Câmera bloqueada. roomId=" + roomId + " userId=" + participantId);
    }

    @Override
    public void removeParticipant(int roomId, int participantId, int moderatorId) {
        ConferenceRoom room = getExistingRoom(roomId);
        requireModerator(room, moderatorId);

        RoomParticipant participant = getParticipantInRoom(roomId, participantId);

        roomParticipantDao.updateStatus(participant.getId(), RoomParticipant.ParticipantStatus.REMOVED);

        // Tenta remover do LiveKit - se falhar (participante desconectado), não é erro crítico
        liveKitService.removeParticipant(buildRoomName(roomId), String.valueOf(participantId));

        logger.log(Level.INFO, "Participante removido da sala. roomId=" + roomId + " userId=" + participantId);
    }

    @Override
    public String generateToken(int roomId, int userId) {
        ConferenceRoom room = getExistingRoom(roomId);

        if (room.getStatus() == ConferenceRoom.RoomStatus.CLOSED) {
            throw new IllegalArgumentException("A sala está encerrada");
        }

        UserModel user = userService.findByid(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        boolean isModerator = user.getRole() == UserModel.UserRole.MODERATOR ||
                user.getRole() == UserModel.UserRole.ADMINISTRATOR;

        RoomParticipant participant = null;

        if (!isModerator) {
            participant = roomParticipantDao.findByRoomAndUser(roomId, userId);
            if (participant == null || participant.getStatus() != RoomParticipant.ParticipantStatus.APPROVED) {
                throw new SecurityException("Acesso negado: participante não aprovado para esta sala");
            }
        } else {
            // Moderador: verifica se é o dono da sala ou ADMINISTRATOR
            if (user.getRole() == UserModel.UserRole.MODERATOR && !room.getModeratorId().equals(userId)) {
                throw new SecurityException("Acesso negado: você não é o moderador desta sala");
            }
            // Cria um participante fictício com permissões completas para o moderador
            participant = new RoomParticipant();
            participant.setCanPublishAudio(true);
            participant.setCanPublishVideo(true);
        }

        String token = liveKitService.generateToken(buildRoomName(roomId), user, participant);
        logger.log(Level.INFO, "Token LiveKit gerado. roomId=" + roomId + " userId=" + userId);
        return token;
    }

    // --- Helpers privados ---

    private ConferenceRoom getExistingRoom(int roomId) {
        ConferenceRoom room = conferenceRoomDao.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Sala não encontrada: " + roomId);
        }
        return room;
    }

    private void requireModerator(ConferenceRoom room, int userId) {
        UserModel user = userService.findByid(userId);
        if (user == null) {
            throw new SecurityException("Usuário não encontrado");
        }
        boolean isAdmin = user.getRole() == UserModel.UserRole.ADMINISTRATOR;
        boolean isRoomModerator = user.getRole() == UserModel.UserRole.MODERATOR
                && room.getModeratorId().equals(userId);

        if (!isAdmin && !isRoomModerator) {
            throw new SecurityException("Acesso negado: apenas o moderador da sala pode executar esta ação");
        }
    }

    private RoomParticipant getParticipantInRoom(int roomId, int userId) {
        RoomParticipant participant = roomParticipantDao.findByRoomAndUser(roomId, userId);
        if (participant == null) {
            throw new IllegalArgumentException("Participante não encontrado na sala");
        }
        return participant;
    }

    private RoomParticipant getApprovedParticipantInRoom(int roomId, int userId) {
        RoomParticipant participant = getParticipantInRoom(roomId, userId);
        if (participant.getStatus() != RoomParticipant.ParticipantStatus.APPROVED) {
            throw new IllegalArgumentException("Participante não está aprovado na sala");
        }
        return participant;
    }

    public static String buildRoomName(int roomId) {
        return "room_" + roomId;
    }
}
