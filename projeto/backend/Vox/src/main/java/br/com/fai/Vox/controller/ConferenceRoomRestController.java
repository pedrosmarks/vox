package br.com.fai.Vox.controller;
import br.com.fai.Vox.domain.enums.UserRoleEnum;

import br.com.fai.Vox.domain.ConferenceRoom;
import br.com.fai.Vox.domain.RoomParticipant;
import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.CreateConferenceRoomDto;
import br.com.fai.Vox.domain.dto.LiveKitTokenDto;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.conferenceroom.ConferenceRoomService;
import br.com.fai.Vox.port.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/salas")
public class ConferenceRoomRestController {

    private final ConferenceRoomService conferenceRoomService;
    private final UserService userService;
    private final AuthenticatedUserHelper authHelper;

    public ConferenceRoomRestController(ConferenceRoomService conferenceRoomService,
                                        UserService userService,
                                        AuthenticatedUserHelper authHelper) {
        this.conferenceRoomService = conferenceRoomService;
        this.userService = userService;
        this.authHelper = authHelper;
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody final CreateConferenceRoomDto data,
                                       HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        int municipalityId = authHelper.getMunicipalityId(request);

        requireRole(userId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);

        final int id = conferenceRoomService.create(data, userId, municipalityId);

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping
    public ResponseEntity<List<ConferenceRoom>> findAll(HttpServletRequest request) {
        int municipalityId = authHelper.getMunicipalityId(request);
        return ResponseEntity.ok(conferenceRoomService.findByMunicipalityId(municipalityId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConferenceRoom> findById(@PathVariable final int id) {
        ConferenceRoom room = conferenceRoomService.findById(id);
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final int id,
                                       HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        conferenceRoomService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/solicitacoes-entrada")
    public ResponseEntity<Void> requestEntry(@PathVariable final int id,
                                             HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        conferenceRoomService.requestEntry(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/solicitacoes-entrada")
    public ResponseEntity<List<RoomParticipant>> findRequests(@PathVariable final int id,
                                                               HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        requireRole(userId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);
        return ResponseEntity.ok(conferenceRoomService.findRequests(id));
    }

    @PostMapping("/{id}/solicitacoes-entrada/{participanteId}/aprovar")
    public ResponseEntity<Void> approveEntry(@PathVariable final int id,
                                              @PathVariable final int participanteId,
                                              HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);
        conferenceRoomService.approveEntry(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/solicitacoes-entrada/{participanteId}/rejeitar")
    public ResponseEntity<Void> rejectEntry(@PathVariable final int id,
                                             @PathVariable final int participanteId,
                                             HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);
        conferenceRoomService.rejectEntry(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/solicitacoes-fala")
    public ResponseEntity<Void> requestToSpeak(@PathVariable final int id,
                                                HttpServletRequest request) {
        conferenceRoomService.requestToSpeak(id, authHelper.getUserId(request));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/solicitacoes-fala")
    public ResponseEntity<List<RoomParticipant>> findSpeechRequests(@PathVariable final int id,
                                                                     HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);
        return ResponseEntity.ok(conferenceRoomService.findSpeechRequests(id));
    }

    @PostMapping("/{id}/solicitacoes-fala/{participanteId}/aprovar")
    public ResponseEntity<Void> approveSpeech(@PathVariable final int id,
                                               @PathVariable final int participanteId,
                                               HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);
        conferenceRoomService.approveSpeech(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/solicitacoes-fala/{participanteId}/rejeitar")
    public ResponseEntity<Void> rejectSpeech(@PathVariable final int id,
                                              @PathVariable final int participanteId,
                                              HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);
        conferenceRoomService.rejectSpeech(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/solicitacoes-fala/{participanteId}/revogar")
    public ResponseEntity<Void> revokeSpeech(@PathVariable final int id,
                                             @PathVariable final int participanteId,
                                             HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);
        conferenceRoomService.revokeSpeech(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/participantes/{participanteId}/microfone/liberar")
    public ResponseEntity<Void> enableMicrophone(@PathVariable final int id,
                                                  @PathVariable final int participanteId,
                                                  HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);
        conferenceRoomService.enableMicrophone(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/participantes/{participanteId}/microfone/bloquear")
    public ResponseEntity<Void> disableMicrophone(@PathVariable final int id,
                                                   @PathVariable final int participanteId,
                                                   HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);
        conferenceRoomService.disableMicrophone(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/participantes/{participanteId}/camera/liberar")
    public ResponseEntity<Void> enableCamera(@PathVariable final int id,
                                              @PathVariable final int participanteId,
                                              HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);
        conferenceRoomService.enableCamera(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/participantes/{participanteId}/camera/bloquear")
    public ResponseEntity<Void> disableCamera(@PathVariable final int id,
                                               @PathVariable final int participanteId,
                                               HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);
        conferenceRoomService.disableCamera(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/participantes/{participanteId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable final int id,
                                                   @PathVariable final int participanteId,
                                                   HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserRoleEnum.MODERATOR, UserRoleEnum.ADMINISTRATOR);
        conferenceRoomService.removeParticipant(id, participanteId, moderatorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/token")
    public ResponseEntity<LiveKitTokenDto> generateToken(@PathVariable final int id,
                                                          HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        String token = conferenceRoomService.generateToken(id, userId);
        return ResponseEntity.ok(new LiveKitTokenDto(token));
    }

    private void requireRole(int userId, UserRoleEnum... allowedRoles) {
        UserModel user = userService.findByid(userId);
        if (user == null) {
            throw new SecurityException("Usuário não encontrado");
        }
        for (UserRoleEnum role : allowedRoles) {
            if (user.getRole() == role) {
                return;
            }
        }
        throw new SecurityException("Acesso negado: permissão insuficiente para esta operação");
    }
}
