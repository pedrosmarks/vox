package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.dto.ForgotPasswordDto;
import br.com.fai.Vox.domain.dto.ResetPasswordDto;
import br.com.fai.Vox.port.service.user.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Profile("jwt")
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserService userService;

    public AuthRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody final ForgotPasswordDto data) {
        final boolean response = userService.forgotPassword(data.getEmail());
        return response ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody final ResetPasswordDto data) {
        final boolean response = userService.resetPassword(data.getToken(), data.getNewPassword());
        return response ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
}
