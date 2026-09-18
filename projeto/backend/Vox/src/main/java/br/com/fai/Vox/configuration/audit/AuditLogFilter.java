package br.com.fai.Vox.configuration.audit;

import br.com.fai.Vox.domain.AuditLog;
import br.com.fai.Vox.implementation.service.authentication.jwt.JwtService;
import br.com.fai.Vox.port.service.auditlog.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Filtro de auditoria: registra chamadas de escrita (POST/PUT/PATCH/DELETE) e a
 * autenticação (/authenticate). GETs e a documentação (Swagger) são ignorados.
 *
 * <p>Não guarda o corpo da requisição, apenas metadados da atividade. A gravação
 * é assíncrona ({@link AuditLogService#record}) e nunca interrompe a resposta.</p>
 */
@Profile("jwt")
@Component
public class AuditLogFilter extends OncePerRequestFilter {

    private static final Set<String> AUDITED_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditLogService auditLogService;
    private final JwtService jwtService;

    public AuditLogFilter(AuditLogService auditLogService, JwtService jwtService) {
        this.auditLogService = auditLogService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String errorMessage = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
            throw ex;
        } finally {
            try {
                if (shouldAudit(request)) {
                    long duration = System.currentTimeMillis() - start;
                    auditLogService.record(buildEntry(request, response, duration, errorMessage));
                }
            } catch (Exception auditEx) {
                // Auditoria jamais deve afetar a resposta ao cliente.
                logger.warn("Falha ao registrar auditoria da requisição", auditEx);
            }
        }
    }

    /**
     * Audita apenas mutações e o login. Ignora GET/HEAD/OPTIONS e a documentação.
     */
    private boolean shouldAudit(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path != null && (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html"))) {
            return false;
        }
        String method = request.getMethod();
        if (AUDITED_METHODS.contains(method)) {
            return true;
        }
        // Login: POST já é coberto acima; /authenticate é POST, então entra na regra.
        return false;
    }

    private AuditLog buildEntry(HttpServletRequest request, HttpServletResponse response,
                                long duration, String errorMessage) {
        AuditLog log = new AuditLog();
        log.setHttpMethod(request.getMethod());
        log.setPath(truncate(request.getRequestURI(), 512));
        log.setQueryString(truncate(request.getQueryString(), 1024));

        int status = response.getStatus();
        log.setStatusCode(status);
        log.setSuccess(status >= 200 && status < 400);
        log.setDurationMs(duration);
        log.setIpAddress(truncate(resolveClientIp(request), 64));
        log.setUserAgent(truncate(request.getHeader("User-Agent"), 512));
        log.setErrorMessage(truncate(errorMessage, 1024));

        // Usuário: extraído diretamente do token (quando presente e válido).
        // Chamadas não autenticadas (login, forgot-password) ficam com user nulo.
        populateUserFromToken(request, log);
        return log;
    }

    private void populateUserFromToken(HttpServletRequest request, AuditLog log) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return;
        }
        try {
            String token = header.substring(7);
            log.setUserId(jwtService.getUserIdFromToken(token));
            log.setMunicipalityId(jwtService.getMunicipalityIdFromToken(token));
            Object role = jwtService.getAllClaimsFromToken(token).get("role");
            if (role != null) log.setUserRole(role.toString());
        } catch (Exception ignored) {
            // Token ausente, expirado ou inválido: registra sem identificação de usuário.
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // Primeiro IP da cadeia é o cliente original.
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
