package br.com.fai.Vox.controller;

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

    // --- CRUD de salas ---

    /**
     * POST /api/salas
     * Apenas MODERADOR ou ADMINISTRATOR pode criar salas.
     */
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody final CreateConferenceRoomDto data,
                                       HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        int municipalityId = authHelper.getMunicipalityId(request);

        requireRole(userId, UserModel.UserRole.MODERATOR, UserModel.UserRole.ADMINISTRATOR);

        final int id = conferenceRoomService.create(data, userId, municipalityId);

        final URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    /**
     * GET /api/salas
     * Qualquer usuário autenticado pode listar salas do seu município.
     */
    @GetMapping
    public ResponseEntity<List<ConferenceRoom>> findAll(HttpServletRequest request) {
        int municipalityId = authHelper.getMunicipalityId(request);
        return ResponseEntity.ok(conferenceRoomService.findByMunicipalityId(municipalityId));
    }

    /**
     * GET /api/salas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConferenceRoom> findById(@PathVariable final int id) {
        ConferenceRoom room = conferenceRoomService.findById(id);
        return ResponseEntity.ok(room);
    }

    /**
     * DELETE /api/salas/{id}
     * Apenas o moderador da sala ou ADMINISTRATOR pode encerrar.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final int id,
                                       HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        conferenceRoomService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // --- Solicitações de entrada ---

    /**
     * POST /api/salas/{id}/solicitacoes-entrada
     * Cidadão solicita entrada na sala.
     */
    @PostMapping("/{id}/solicitacoes-entrada")
    public ResponseEntity<Void> requestEntry(@PathVariable final int id,
                                             HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        conferenceRoomService.requestEntry(id, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/salas/{id}/solicitacoes-entrada
     * Apenas MODERADOR ou ADMINISTRATOR pode ver solicitações pendentes.
     */
    @GetMapping("/{id}/solicitacoes-entrada")
    public ResponseEntity<List<RoomParticipant>> findRequests(@PathVariable final int id,
                                                               HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        requireRole(userId, UserModel.UserRole.MODERATOR, UserModel.UserRole.ADMINISTRATOR);
        return ResponseEntity.ok(conferenceRoomService.findRequests(id));
    }

    /**
     * POST /api/salas/{id}/solicitacoes-entrada/{participanteId}/aprovar
     */
    @PostMapping("/{id}/solicitacoes-entrada/{participanteId}/aprovar")
    public ResponseEntity<Void> approveEntry(@PathVariable final int id,
                                              @PathVariable final int participanteId,
                                              HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserModel.UserRole.MODERATOR, UserModel.UserRole.ADMINISTRATOR);
        conferenceRoomService.approveEntry(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/salas/{id}/solicitacoes-entrada/{participanteId}/rejeitar
     */
    @PostMapping("/{id}/solicitacoes-entrada/{participanteId}/rejeitar")
    public ResponseEntity<Void> rejectEntry(@PathVariable final int id,
                                             @PathVariable final int participanteId,
                                             HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserModel.UserRole.MODERATOR, UserModel.UserRole.ADMINISTRATOR);
        conferenceRoomService.rejectEntry(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    // --- Controle de microfone ---

    /**
     * POST /api/salas/{id}/participantes/{participanteId}/microfone/liberar
     */
    @PostMapping("/{id}/participantes/{participanteId}/microfone/liberar")
    public ResponseEntity<Void> enableMicrophone(@PathVariable final int id,
                                                  @PathVariable final int participanteId,
                                                  HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserModel.UserRole.MODERATOR, UserModel.UserRole.ADMINISTRATOR);
        conferenceRoomService.enableMicrophone(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/salas/{id}/participantes/{participanteId}/microfone/bloquear
     */
    @PostMapping("/{id}/participantes/{participanteId}/microfone/bloquear")
    public ResponseEntity<Void> disableMicrophone(@PathVariable final int id,
                                                   @PathVariable final int participanteId,
                                                   HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserModel.UserRole.MODERATOR, UserModel.UserRole.ADMINISTRATOR);
        conferenceRoomService.disableMicrophone(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    // --- Controle de câmera ---

    /**
     * POST /api/salas/{id}/participantes/{participanteId}/camera/liberar
     */
    @PostMapping("/{id}/participantes/{participanteId}/camera/liberar")
    public ResponseEntity<Void> enableCamera(@PathVariable final int id,
                                              @PathVariable final int participanteId,
                                              HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserModel.UserRole.MODERATOR, UserModel.UserRole.ADMINISTRATOR);
        conferenceRoomService.enableCamera(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/salas/{id}/participantes/{participanteId}/camera/bloquear
     */
    @PostMapping("/{id}/participantes/{participanteId}/camera/bloquear")
    public ResponseEntity<Void> disableCamera(@PathVariable final int id,
                                               @PathVariable final int participanteId,
                                               HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserModel.UserRole.MODERATOR, UserModel.UserRole.ADMINISTRATOR);
        conferenceRoomService.disableCamera(id, participanteId, moderatorId);
        return ResponseEntity.ok().build();
    }

    // --- Expulsão ---

    /**
     * DELETE /api/salas/{id}/participantes/{participanteId}
     */
    @DeleteMapping("/{id}/participantes/{participanteId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable final int id,
                                                   @PathVariable final int participanteId,
                                                   HttpServletRequest request) {
        int moderatorId = authHelper.getUserId(request);
        requireRole(moderatorId, UserModel.UserRole.MODERATOR, UserModel.UserRole.ADMINISTRATOR);
        conferenceRoomService.removeParticipant(id, participanteId, moderatorId);
        return ResponseEntity.noContent().build();
    }

    // --- Token LiveKit ---

    /**
     * POST /api/salas/{id}/token
     * Gera o token LiveKit para o usuário autenticado entrar na sala.
     * Cidadão só recebe token se estiver aprovado.
     * Moderador recebe token com permissões administrativas.
     */
    @PostMapping("/{id}/token")
    public ResponseEntity<LiveKitTokenDto> generateToken(@PathVariable final int id,
                                                          HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        String token = conferenceRoomService.generateToken(id, userId);
        return ResponseEntity.ok(new LiveKitTokenDto(token));
    }

    // --- Helper de autorização por role ---

    private void requireRole(int userId, UserModel.UserRole... allowedRoles) {
        UserModel user = userService.findByid(userId);
        if (user == null) {
            throw new SecurityException("Usuário não encontrado");
        }
        for (UserModel.UserRole role : allowedRoles) {
            if (user.getRole() == role) {
                return;
            }
        }
        throw new SecurityException("Acesso negado: permissão insuficiente para esta operação");
    }
}
