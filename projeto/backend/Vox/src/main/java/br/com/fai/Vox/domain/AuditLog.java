package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro de auditoria de uma chamada à API. Cobre ações de escrita
 * (POST/PUT/PATCH/DELETE) e autenticação. Não guarda o corpo da requisição,
 * apenas metadados da atividade realizada.
 */
@Getter
@Setter
public class AuditLog {

    private Long id;
    /** ID do usuário autenticado; {@code null} em chamadas não autenticadas (ex.: login). */
    private Integer userId;
    private String userRole;
    private Integer municipalityId;
    private String httpMethod;
    private String path;
    private String queryString;
    private int statusCode;
    private boolean success;
    private Long durationMs;
    private String ipAddress;
    private String userAgent;
    private String errorMessage;
    private LocalDateTime createdAt;

    public AuditLog() {}
}
