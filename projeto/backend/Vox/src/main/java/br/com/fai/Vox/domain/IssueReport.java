package br.com.fai.Vox.domain;

import br.com.fai.Vox.domain.enuns.ModerationStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class IssueReport {

    private Integer id;
    private Integer municipalityId;
    private Integer authorId;
    private Integer councilorId;
    private String title;
    private String description;
    private String neighborhood;
    private String street;
    private String number;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private IssueStatus status;
    private ModerationStatus moderationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum IssueStatus {
        OPEN,
        IN_PROGRESS,
        RESOLVED,
        CLOSED
    }

    public IssueReport() {}
}
