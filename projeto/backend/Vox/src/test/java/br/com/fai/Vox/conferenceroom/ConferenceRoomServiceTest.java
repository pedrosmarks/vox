package br.com.fai.Vox.conferenceroom;

import br.com.fai.Vox.domain.ConferenceRoom;
import br.com.fai.Vox.domain.RoomParticipant;
import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.CreateConferenceRoomDto;
import br.com.fai.Vox.implementation.service.conferenceroom.ConferenceRoomServiceImpl;
import br.com.fai.Vox.port.dao.conferenceroom.ConferenceRoomDao;
import br.com.fai.Vox.port.dao.roomparticipant.RoomParticipantDao;
import br.com.fai.Vox.port.service.livekit.LiveKitService;
import br.com.fai.Vox.port.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConferenceRoomService - Testes de Autorização e Permissões")
class ConferenceRoomServiceTest {

    @Mock
    private ConferenceRoomDao conferenceRoomDao;
    @Mock
    private RoomParticipantDao roomParticipantDao;
    @Mock
    private LiveKitService liveKitService;
    @Mock
    private UserService userService;

    @InjectMocks
    private ConferenceRoomServiceImpl service;

    private static final int MODERATOR_ID = 1;
    private static final int CITIZEN_ID = 2;
    private static final int OTHER_MODERATOR_ID = 3;
    private static final int ADMIN_ID = 4;
    private static final int ROOM_ID = 10;

    private ConferenceRoom openRoom;
    private ConferenceRoom closedRoom;
    private UserModel moderatorUser;
    private UserModel citizenUser;
    private UserModel adminUser;
    private UserModel otherModeratorUser;

    @BeforeEach
    void setUp() {
        openRoom = new ConferenceRoom();
        openRoom.setId(ROOM_ID);
        openRoom.setName("Audiência Pública 1");
        openRoom.setModeratorId(MODERATOR_ID);
        openRoom.setMunicipalityId(100);
        openRoom.setStatus(ConferenceRoom.RoomStatus.OPEN);

        closedRoom = new ConferenceRoom();
        closedRoom.setId(ROOM_ID);
        closedRoom.setName("Sala Encerrada");
        closedRoom.setModeratorId(MODERATOR_ID);
        closedRoom.setMunicipalityId(100);
        closedRoom.setStatus(ConferenceRoom.RoomStatus.CLOSED);

        moderatorUser = new UserModel();
        moderatorUser.setId(MODERATOR_ID);
        moderatorUser.setName("Moderador");
        moderatorUser.setRole(UserModel.UserRole.MODERATOR);
        moderatorUser.setMunicipalityId(100);

        citizenUser = new UserModel();
        citizenUser.setId(CITIZEN_ID);
        citizenUser.setName("Cidadão");
        citizenUser.setRole(UserModel.UserRole.CITIZEN);
        citizenUser.setMunicipalityId(100);

        adminUser = new UserModel();
        adminUser.setId(ADMIN_ID);
        adminUser.setName("Administrador");
        adminUser.setRole(UserModel.UserRole.ADMINISTRATOR);
        adminUser.setMunicipalityId(100);

        otherModeratorUser = new UserModel();
        otherModeratorUser.setId(OTHER_MODERATOR_ID);
        otherModeratorUser.setName("Outro Moderador");
        otherModeratorUser.setRole(UserModel.UserRole.MODERATOR);
        otherModeratorUser.setMunicipalityId(100);
    }

    // =============================
    // Criação de sala
    // =============================

    @Test
    @DisplayName("Criar sala com dados válidos deve retornar ID positivo")
    void createRoom_validData_returnsId() {
        CreateConferenceRoomDto dto = new CreateConferenceRoomDto();
        dto.setName("Nova Sala");

        when(conferenceRoomDao.create(any())).thenReturn(ROOM_ID);
        doNothing().when(liveKitService).createRoom(anyString());

        int id = service.create(dto, MODERATOR_ID, 100);

        assertEquals(ROOM_ID, id);
        verify(conferenceRoomDao).create(any());
        verify(liveKitService).createRoom("room_" + ROOM_ID);
    }

    @Test
    @DisplayName("Criar sala sem nome deve lançar IllegalArgumentException")
    void createRoom_emptyName_throwsIllegalArgument() {
        CreateConferenceRoomDto dto = new CreateConferenceRoomDto();
        dto.setName("");

        assertThrows(IllegalArgumentException.class,
                () -> service.create(dto, MODERATOR_ID, 100));
        verifyNoInteractions(conferenceRoomDao);
    }

    // =============================
    // Controle de acesso à sala
    // =============================

    @Test
    @DisplayName("Moderador da sala pode encerrá-la")
    void deleteRoom_byModerator_succeeds() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(MODERATOR_ID)).thenReturn(moderatorUser);

        assertDoesNotThrow(() -> service.delete(ROOM_ID, MODERATOR_ID));
        verify(conferenceRoomDao).updateStatus(ROOM_ID, ConferenceRoom.RoomStatus.CLOSED);
    }

    @Test
    @DisplayName("ADMINISTRATOR pode encerrar qualquer sala")
    void deleteRoom_byAdmin_succeeds() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(ADMIN_ID)).thenReturn(adminUser);

        assertDoesNotThrow(() -> service.delete(ROOM_ID, ADMIN_ID));
        verify(conferenceRoomDao).updateStatus(ROOM_ID, ConferenceRoom.RoomStatus.CLOSED);
    }

    @Test
    @DisplayName("Cidadão NÃO pode encerrar sala - deve lançar SecurityException")
    void deleteRoom_byCitizen_throwsSecurityException() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(CITIZEN_ID)).thenReturn(citizenUser);

        assertThrows(SecurityException.class,
                () -> service.delete(ROOM_ID, CITIZEN_ID));
        verify(conferenceRoomDao, never()).updateStatus(anyInt(), any());
    }

    @Test
    @DisplayName("Moderador de outra sala NÃO pode encerrar esta sala")
    void deleteRoom_byOtherModerator_throwsSecurityException() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(OTHER_MODERATOR_ID)).thenReturn(otherModeratorUser);

        assertThrows(SecurityException.class,
                () -> service.delete(ROOM_ID, OTHER_MODERATOR_ID));
    }

    // =============================
    // Solicitação de entrada
    // =============================

    @Test
    @DisplayName("Cidadão pode solicitar entrada em sala aberta")
    void requestEntry_openRoom_succeeds() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(roomParticipantDao.findByRoomAndUser(ROOM_ID, CITIZEN_ID)).thenReturn(null);
        when(roomParticipantDao.create(ROOM_ID, CITIZEN_ID)).thenReturn(1);

        assertDoesNotThrow(() -> service.requestEntry(ROOM_ID, CITIZEN_ID));
        verify(roomParticipantDao).create(ROOM_ID, CITIZEN_ID);
    }

    @Test
    @DisplayName("Cidadão NÃO pode solicitar entrada em sala fechada")
    void requestEntry_closedRoom_throwsIllegalArgument() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(closedRoom);

        assertThrows(IllegalArgumentException.class,
                () -> service.requestEntry(ROOM_ID, CITIZEN_ID));
        verify(roomParticipantDao, never()).create(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Cidadão com solicitação PENDING não pode solicitar novamente")
    void requestEntry_alreadyPending_throwsIllegalArgument() {
        RoomParticipant pending = new RoomParticipant();
        pending.setStatus(RoomParticipant.ParticipantStatus.PENDING);

        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(roomParticipantDao.findByRoomAndUser(ROOM_ID, CITIZEN_ID)).thenReturn(pending);

        assertThrows(IllegalArgumentException.class,
                () -> service.requestEntry(ROOM_ID, CITIZEN_ID));
    }

    // =============================
    // Aprovação / Rejeição
    // =============================

    @Test
    @DisplayName("Moderador pode aprovar solicitação PENDING")
    void approveEntry_pendingRequest_succeeds() {
        RoomParticipant pending = new RoomParticipant();
        pending.setId(50);
        pending.setUserId(CITIZEN_ID);
        pending.setStatus(RoomParticipant.ParticipantStatus.PENDING);

        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(MODERATOR_ID)).thenReturn(moderatorUser);
        when(roomParticipantDao.findByRoomAndUser(ROOM_ID, CITIZEN_ID)).thenReturn(pending);

        assertDoesNotThrow(() -> service.approveEntry(ROOM_ID, CITIZEN_ID, MODERATOR_ID));
        verify(roomParticipantDao).updateStatus(50, RoomParticipant.ParticipantStatus.APPROVED);
    }

    @Test
    @DisplayName("Cidadão NÃO pode aprovar solicitação - deve lançar SecurityException")
    void approveEntry_byCitizen_throwsSecurityException() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(CITIZEN_ID)).thenReturn(citizenUser);

        assertThrows(SecurityException.class,
                () -> service.approveEntry(ROOM_ID, CITIZEN_ID, CITIZEN_ID));
        verify(roomParticipantDao, never()).updateStatus(anyInt(), any());
    }

    @Test
    @DisplayName("Não pode aprovar solicitação que não está PENDING")
    void approveEntry_notPending_throwsIllegalArgument() {
        RoomParticipant approved = new RoomParticipant();
        approved.setId(50);
        approved.setStatus(RoomParticipant.ParticipantStatus.APPROVED);

        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(MODERATOR_ID)).thenReturn(moderatorUser);
        when(roomParticipantDao.findByRoomAndUser(ROOM_ID, CITIZEN_ID)).thenReturn(approved);

        assertThrows(IllegalArgumentException.class,
                () -> service.approveEntry(ROOM_ID, CITIZEN_ID, MODERATOR_ID));
    }

    // =============================
    // Controle de microfone
    // =============================

    @Test
    @DisplayName("Moderador pode liberar microfone de participante aprovado")
    void enableMicrophone_approvedParticipant_succeeds() {
        RoomParticipant approved = new RoomParticipant();
        approved.setId(50);
        approved.setStatus(RoomParticipant.ParticipantStatus.APPROVED);
        approved.setCanPublishAudio(false);
        approved.setCanPublishVideo(false);

        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(MODERATOR_ID)).thenReturn(moderatorUser);
        when(roomParticipantDao.findByRoomAndUser(ROOM_ID, CITIZEN_ID)).thenReturn(approved);
        doNothing().when(liveKitService).updateParticipantPermissions(anyString(), anyString(), anyBoolean(), anyBoolean());

        assertDoesNotThrow(() -> service.enableMicrophone(ROOM_ID, CITIZEN_ID, MODERATOR_ID));

        verify(roomParticipantDao).updatePermissions(50, true, false);
        verify(liveKitService).updateParticipantPermissions("room_" + ROOM_ID, String.valueOf(CITIZEN_ID), true, false);
    }

    @Test
    @DisplayName("Cidadão NÃO pode liberar microfone - deve lançar SecurityException")
    void enableMicrophone_byCitizen_throwsSecurityException() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(CITIZEN_ID)).thenReturn(citizenUser);

        assertThrows(SecurityException.class,
                () -> service.enableMicrophone(ROOM_ID, CITIZEN_ID, CITIZEN_ID));
        verify(roomParticipantDao, never()).updatePermissions(anyInt(), anyBoolean(), anyBoolean());
    }

    @Test
    @DisplayName("Não pode liberar microfone de participante não aprovado")
    void enableMicrophone_notApproved_throwsIllegalArgument() {
        RoomParticipant pending = new RoomParticipant();
        pending.setId(50);
        pending.setStatus(RoomParticipant.ParticipantStatus.PENDING);

        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(MODERATOR_ID)).thenReturn(moderatorUser);
        when(roomParticipantDao.findByRoomAndUser(ROOM_ID, CITIZEN_ID)).thenReturn(pending);

        assertThrows(IllegalArgumentException.class,
                () -> service.enableMicrophone(ROOM_ID, CITIZEN_ID, MODERATOR_ID));
    }

    // =============================
    // Controle de câmera
    // =============================

    @Test
    @DisplayName("Moderador pode liberar câmera sem afetar estado do microfone")
    void enableCamera_doesNotChangeAudioPermission() {
        RoomParticipant approved = new RoomParticipant();
        approved.setId(50);
        approved.setStatus(RoomParticipant.ParticipantStatus.APPROVED);
        approved.setCanPublishAudio(true);  // já tinha áudio liberado
        approved.setCanPublishVideo(false);

        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(MODERATOR_ID)).thenReturn(moderatorUser);
        when(roomParticipantDao.findByRoomAndUser(ROOM_ID, CITIZEN_ID)).thenReturn(approved);
        doNothing().when(liveKitService).updateParticipantPermissions(anyString(), anyString(), anyBoolean(), anyBoolean());

        assertDoesNotThrow(() -> service.enableCamera(ROOM_ID, CITIZEN_ID, MODERATOR_ID));

        // Áudio deve continuar true, vídeo deve virar true
        verify(roomParticipantDao).updatePermissions(50, true, true);
        verify(liveKitService).updateParticipantPermissions("room_" + ROOM_ID, String.valueOf(CITIZEN_ID), true, true);
    }

    @Test
    @DisplayName("Bloquear câmera não deve afetar permissão de microfone")
    void disableCamera_doesNotChangeAudioPermission() {
        RoomParticipant approved = new RoomParticipant();
        approved.setId(50);
        approved.setStatus(RoomParticipant.ParticipantStatus.APPROVED);
        approved.setCanPublishAudio(true);  // áudio liberado
        approved.setCanPublishVideo(true);  // câmera liberada

        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(MODERATOR_ID)).thenReturn(moderatorUser);
        when(roomParticipantDao.findByRoomAndUser(ROOM_ID, CITIZEN_ID)).thenReturn(approved);
        doNothing().when(liveKitService).updateParticipantPermissions(anyString(), anyString(), anyBoolean(), anyBoolean());

        assertDoesNotThrow(() -> service.disableCamera(ROOM_ID, CITIZEN_ID, MODERATOR_ID));

        // Áudio deve continuar true, vídeo deve virar false
        verify(roomParticipantDao).updatePermissions(50, true, false);
        verify(liveKitService).updateParticipantPermissions("room_" + ROOM_ID, String.valueOf(CITIZEN_ID), true, false);
    }

    // =============================
    // Expulsão
    // =============================

    @Test
    @DisplayName("Moderador pode expulsar participante da sala")
    void removeParticipant_byModerator_succeeds() {
        RoomParticipant approved = new RoomParticipant();
        approved.setId(50);
        approved.setStatus(RoomParticipant.ParticipantStatus.APPROVED);

        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(MODERATOR_ID)).thenReturn(moderatorUser);
        when(roomParticipantDao.findByRoomAndUser(ROOM_ID, CITIZEN_ID)).thenReturn(approved);
        doNothing().when(liveKitService).removeParticipant(anyString(), anyString());

        assertDoesNotThrow(() -> service.removeParticipant(ROOM_ID, CITIZEN_ID, MODERATOR_ID));

        verify(roomParticipantDao).updateStatus(50, RoomParticipant.ParticipantStatus.REMOVED);
        verify(liveKitService).removeParticipant("room_" + ROOM_ID, String.valueOf(CITIZEN_ID));
    }

    @Test
    @DisplayName("Cidadão NÃO pode expulsar outro cidadão")
    void removeParticipant_byCitizen_throwsSecurityException() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(CITIZEN_ID)).thenReturn(citizenUser);

        assertThrows(SecurityException.class,
                () -> service.removeParticipant(ROOM_ID, CITIZEN_ID, CITIZEN_ID));
        verify(roomParticipantDao, never()).updateStatus(anyInt(), any());
    }

    // =============================
    // Geração de token LiveKit
    // =============================

    @Test
    @DisplayName("Moderador da sala recebe token com permissões completas")
    void generateToken_moderator_succeeds() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(MODERATOR_ID)).thenReturn(moderatorUser);
        when(liveKitService.generateToken(anyString(), any(), any())).thenReturn("mock-token");

        String token = service.generateToken(ROOM_ID, MODERATOR_ID);

        assertNotNull(token);
        assertEquals("mock-token", token);
    }

    @Test
    @DisplayName("Cidadão aprovado recebe token válido")
    void generateToken_approvedCitizen_succeeds() {
        RoomParticipant approved = new RoomParticipant();
        approved.setStatus(RoomParticipant.ParticipantStatus.APPROVED);
        approved.setCanPublishAudio(false);
        approved.setCanPublishVideo(false);

        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(CITIZEN_ID)).thenReturn(citizenUser);
        when(roomParticipantDao.findByRoomAndUser(ROOM_ID, CITIZEN_ID)).thenReturn(approved);
        when(liveKitService.generateToken(anyString(), any(), any())).thenReturn("citizen-token");

        String token = service.generateToken(ROOM_ID, CITIZEN_ID);

        assertNotNull(token);
        assertEquals("citizen-token", token);
    }

    @Test
    @DisplayName("Cidadão NÃO aprovado NÃO recebe token - deve lançar SecurityException")
    void generateToken_notApprovedCitizen_throwsSecurityException() {
        RoomParticipant pending = new RoomParticipant();
        pending.setStatus(RoomParticipant.ParticipantStatus.PENDING);

        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(CITIZEN_ID)).thenReturn(citizenUser);
        when(roomParticipantDao.findByRoomAndUser(ROOM_ID, CITIZEN_ID)).thenReturn(pending);

        assertThrows(SecurityException.class,
                () -> service.generateToken(ROOM_ID, CITIZEN_ID));
        verify(liveKitService, never()).generateToken(anyString(), any(), any());
    }

    @Test
    @DisplayName("Cidadão sem solicitação NÃO recebe token")
    void generateToken_noRequest_throwsSecurityException() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(CITIZEN_ID)).thenReturn(citizenUser);
        when(roomParticipantDao.findByRoomAndUser(ROOM_ID, CITIZEN_ID)).thenReturn(null);

        assertThrows(SecurityException.class,
                () -> service.generateToken(ROOM_ID, CITIZEN_ID));
    }

    @Test
    @DisplayName("Token não é gerado para sala encerrada")
    void generateToken_closedRoom_throwsIllegalArgument() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(closedRoom);

        assertThrows(IllegalArgumentException.class,
                () -> service.generateToken(ROOM_ID, CITIZEN_ID));
    }

    @Test
    @DisplayName("Moderador de outra sala NÃO recebe token desta sala")
    void generateToken_wrongModerator_throwsSecurityException() {
        when(conferenceRoomDao.findById(ROOM_ID)).thenReturn(openRoom);
        when(userService.findByid(OTHER_MODERATOR_ID)).thenReturn(otherModeratorUser);

        assertThrows(SecurityException.class,
                () -> service.generateToken(ROOM_ID, OTHER_MODERATOR_ID));
    }
}
