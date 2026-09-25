package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.fai.Vox.domain.enums.ModerationStatusEnum;
import br.com.fai.Vox.domain.enums.ProjectNatureEnum;
import br.com.fai.Vox.domain.enums.ProjectStatusEnum;
import br.com.fai.Vox.domain.enums.ProjectTypeEnum;

@Getter
@Setter
public class Project {

    private Integer id;
    private Integer municipalityId;
    private Integer categoryId;
    private ProjectTypeEnum type;
    private ProjectNatureEnum nature;
    private String title;
    private String description;
    private ProjectStatusEnum status;
    private ModerationStatusEnum moderationStatus;
    private Integer authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean highlighted;
    private Boolean isOfficial;
    private String neighborhood;
    private String street;
    private String number;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDate endDate;
    private String financialAnalysis;
    private BigDecimal estimatedCost;
    private BigDecimal approvedBudget;

    public Project() {
    }

    public Project(Integer id, Integer municipalityId, Integer categoryId, ProjectTypeEnum type, String title,
                   String description, ProjectStatusEnum status, Integer authorId, LocalDateTime createdAt,
                   LocalDateTime updatedAt, Boolean highlighted, Boolean isOfficial, String neighborhood,
                   String street, String number, BigDecimal latitude, BigDecimal longitude, LocalDate startDate,
                   LocalDate expectedEndDate, LocalDate endDate, String financialAnalysis,
                   BigDecimal estimatedCost, BigDecimal approvedBudget) {
        this.id = id;
        this.municipalityId = municipalityId;
        this.categoryId = categoryId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.status = status;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.highlighted = highlighted;
        this.isOfficial = isOfficial;
        this.neighborhood = neighborhood;
        this.street = street;
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startDate = startDate;
        this.expectedEndDate = expectedEndDate;
        this.endDate = endDate;
        this.financialAnalysis = financialAnalysis;
        this.estimatedCost = estimatedCost;
        this.approvedBudget = approvedBudget;
    }

}
