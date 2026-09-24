package br.com.fai.Vox.controller;
import br.com.fai.Vox.domain.enums.UserRoleEnum;

import br.com.fai.Vox.domain.AuditLog;
import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.PageResponse;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.auditlog.AuditLogService;
import br.com.fai.Vox.port.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Consulta dos logs de auditoria. Restrito a ADMINISTRATOR e sempre escopado ao
 * município do administrador autenticado (via token).
 */
@RestController
@RequestMapping("/api/admin/logs")
public class AuditLogRestController {

    private final AuditLogService auditLogService;
    private final UserService userService;
    private final AuthenticatedUserHelper authHelper;

    public AuditLogRestController(AuditLogService auditLogService,
                                  UserService userService,
                                  AuthenticatedUserHelper authHelper) {
        this.auditLogService = auditLogService;
        this.userService = userService;
        this.authHelper = authHelper;
    }

    /**
     * GET /api/admin/logs?page=0&size=20&userId=7&method=POST&from=2026-09-01&to=2026-09-16
     * Lista os registros de auditoria do município, ordenados do mais recente.
     *
     * @param userId filtra por usuário (opcional)
     * @param method filtra por método HTTP: POST, PUT, PATCH ou DELETE (opcional)
     * @param from   limite inferior de data (opcional, ISO yyyy-MM-dd)
     * @param to     limite superior de data (opcional, ISO yyyy-MM-dd)
     */
    @GetMapping
    public ResponseEntity<PageResponse<AuditLog>> find(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        int municipalityId = requireAdminMunicipality(request);
        return ResponseEntity.ok(
                auditLogService.find(municipalityId, userId, method, from, to, page, size));
    }

    private int requireAdminMunicipality(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        UserModel user = userService.findByid(userId);
        if (user == null || user.getRole() != UserRoleEnum.ADMINISTRATOR) {
            throw new SecurityException("Acesso negado: apenas administradores podem acessar os logs");
        }
        return authHelper.getMunicipalityId(request);
    }
}
