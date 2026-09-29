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

    @GetMapping
    public ResponseEntity<UserSettings> getMySettings(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        return ResponseEntity.ok(userSettingsService.findByUserId(userId));
    }

    @PutMapping
    public ResponseEntity<Void> updateMySettings(@Valid @RequestBody UpdateUserSettingsDto dto,
                                                  HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        userSettingsService.update(userId, dto);
        return ResponseEntity.noContent().build();
    }
}
