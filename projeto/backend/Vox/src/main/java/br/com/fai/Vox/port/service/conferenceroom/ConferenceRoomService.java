package br.com.fai.Vox.port.service.conferenceroom;

import br.com.fai.Vox.domain.ConferenceRoom;
import br.com.fai.Vox.domain.RoomParticipant;
import br.com.fai.Vox.domain.dto.CreateConferenceRoomDto;

import java.util.List;

public interface ConferenceRoomService {

    int create(CreateConferenceRoomDto dto, int moderatorId, int municipalityId);

    void delete(int roomId, int requestingUserId);

    ConferenceRoom findById(int roomId);

    List<ConferenceRoom> findByMunicipalityId(int municipalityId);

    // --- Solicitações de entrada ---

    void requestEntry(int roomId, int userId);

    List<RoomParticipant> findRequests(int roomId);

    void approveEntry(int roomId, int participantId, int moderatorId);

    void rejectEntry(int roomId, int participantId, int moderatorId);

    // --- Controle de permissões ---

    void enableMicrophone(int roomId, int participantId, int moderatorId);

    void disableMicrophone(int roomId, int participantId, int moderatorId);

    void enableCamera(int roomId, int participantId, int moderatorId);

    void disableCamera(int roomId, int participantId, int moderatorId);

    // --- Remoção ---

    void removeParticipant(int roomId, int participantId, int moderatorId);

    // --- Token LiveKit ---

    String generateToken(int roomId, int userId);
}
