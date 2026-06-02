package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.AuthenticationDto;
import br.com.fai.Vox.port.service.authentication.AuthenticationService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("basic")
@RestController
@RequestMapping("/authenticate")
public class BasicAuthenticationRestController {

    private final AuthenticationService authenticationService;

    public BasicAuthenticationRestController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public ResponseEntity<UserModel> authenticate(@RequestBody final AuthenticationDto authenticationDto) {

        UserModel authenticatedUser = authenticationService.authenticate(
                authenticationDto.getEmail(),
                authenticationDto.getPassword());

        if(authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(authenticatedUser);
    }

}
