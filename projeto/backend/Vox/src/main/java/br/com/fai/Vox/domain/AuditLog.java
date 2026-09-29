package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuditLog {

    private Long id;
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
