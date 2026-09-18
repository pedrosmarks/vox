package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.UserSettings;
import br.com.fai.Vox.domain.dto.UpdateUserSettingsDto;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.usersettings.UserSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class UserSettingsRestController {

    private final UserSettingsService userSettingsService;
    private final AuthenticatedUserHelper authHelper;

    public UserSettingsRestController(UserSettingsService userSettingsService,
                                      AuthenticatedUserHelper authHelper) {
        this.userSettingsService = userSettingsService;
        this.authHelper = authHelper;
    }

    /**
     * Retorna as configurações do usuário autenticado.
     * Se ainda não existirem, retorna os valores padrão (fontSize=16, mode=NONE).
     */
    @GetMapping
    public ResponseEntity<UserSettings> getMySettings(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        return ResponseEntity.ok(userSettingsService.findByUserId(userId));
    }

    /**
     * Salva ou atualiza as configurações do usuário autenticado.
     * Regras:
     *  - fontSize: entre 15 e 30 (obrigatório).
     *  - accessibilityMode: apenas um modo pode estar ativo (NONE desativa todos).
     *    Quando um modo estiver ativo, somente o fontSize pode mudar junto — os outros modos devem ser NONE.
     *    Essa exclusividade é aplicada pelo front; o backend persiste o estado recebido.
     */
    @PutMapping
    public ResponseEntity<Void> updateMySettings(@Valid @RequestBody UpdateUserSettingsDto dto,
                                                  HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        userSettingsService.update(userId, dto);
        return ResponseEntity.noContent().build();
    }
}
