package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.dto.AuthenticatedUserDto;
import br.com.fai.Vox.domain.dto.ForgotPasswordDto;
import br.com.fai.Vox.domain.dto.ResetPasswordDto;
import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.implementation.service.authentication.helper.AuthenticatedUserHelper;
import br.com.fai.Vox.port.service.user.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Profile("jwt")
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserService userService;
    private final AuthenticatedUserHelper authHelper;

    public AuthRestController(UserService userService, AuthenticatedUserHelper authHelper) {
        this.userService = userService;
        this.authHelper = authHelper;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserDto> me(HttpServletRequest request) {
        int userId = authHelper.getUserId(request);
        UserModel user = userService.findByid(userId);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(AuthenticatedUserDto.from(user));
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
