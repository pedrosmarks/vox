package br.com.fai.Vox.implementation.service.authentication.helper;

import br.com.fai.Vox.implementation.service.authentication.jwt.JwtService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Profile("jwt")
@Component
public class AuthenticatedUserHelper {

    private final JwtService jwtService;

    public AuthenticatedUserHelper(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public int getUserId(HttpServletRequest request) {
        return jwtService.getUserIdFromToken(extractToken(request));
    }

    public int getMunicipalityId(HttpServletRequest request) {
        return jwtService.getMunicipalityIdFromToken(extractToken(request));
    }

    public String getEmail(HttpServletRequest request) {
        return jwtService.getEmailFromToken(extractToken(request));
    }

    public String getRole(HttpServletRequest request) {
        return (String) jwtService.getAllClaimsFromToken(extractToken(request)).get("role");
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Token JWT ausente ou inválido.");
        }
        return header.substring(7);
    }
}
