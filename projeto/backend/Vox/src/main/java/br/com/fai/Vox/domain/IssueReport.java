package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fai.Vox.domain.enums.IssueStatusEnum;
import br.com.fai.Vox.domain.enums.ModerationStatusEnum;

@Getter
@Setter
public class IssueReport {

    private Integer id;
    private Integer municipalityId;
    private Integer categoryId;
    private Integer authorId;
    private Integer councilorId;
    private String title;
    private String description;
    private String neighborhood;
    private String street;
    private String number;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private IssueStatusEnum status;
    private ModerationStatusEnum moderationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public IssueReport() {}
}
