package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.AuthenticationDto;
import br.com.fai.Vox.domain.dto.JwtTokenDto;
import br.com.fai.Vox.implementation.service.authentication.jwt.JwtService;
import br.com.fai.Vox.port.service.authentication.AuthenticationService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("jwt")
@RestController
@RequestMapping("/authenticate")
public class JwtAuthenticationRestController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationRestController(AuthenticationService authenticationService, JwtService jwtService, UserDetailsService userDetailsService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping
    public ResponseEntity<JwtTokenDto> authenticate(@RequestBody final AuthenticationDto authenticationDto) {

        UserModel authenticatedUser = authenticationService.authenticate(
                authenticationDto.getEmail(),
                authenticationDto.getPassword());

        if(authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationDto.getEmail());
        final String jwt = jwtService.generateToken(userDetails, authenticatedUser.getName(),
                authenticatedUser.getRole(), authenticatedUser.getEmail());

        if (jwt == null || jwt.isEmpty()) {
            throw new InternalError("Invalido");
        }

        System.out.println("token criado " + jwt);

        JwtTokenDto jwtTokenDto = new JwtTokenDto(jwt);

        return ResponseEntity.ok(jwtTokenDto);
    }

}
