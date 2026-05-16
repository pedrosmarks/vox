package br.com.fai.Vox.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Project {

    private Integer id;
    private Integer municipalityId;
    private Integer categoryId;
    private ProjectType type; // Enum customizado
    private String title;
    private String description;
    private ProjectStatus status; // Enum customizado
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

    public enum ProjectType {
        CITIZEN,
        CHAMBER
    }

    public enum ProjectStatus {
        PENDING_APPROVAL,
        REJECTED,
        PUBLISHED,
        IN_VOTING,
        SELECTED_BY_COUNCIL,
        APPROVED_BY_COUNCIL,
        IN_EXECUTION,
        COMPLETED,
        ARCHIVED,
        CANCELLED
    }

    // Construtor Padrão (Vazio)
    public Project() {
    }

    // Construtor Completo
    public Project(Integer id, Integer municipalityId, Integer categoryId, ProjectType type, String title,
                   String description, ProjectStatus status, Integer authorId, LocalDateTime createdAt,
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

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getMunicipalityId() { return municipalityId; }
    public void setMunicipalityId(Integer municipalityId) { this.municipalityId = municipalityId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public ProjectType getType() { return type; }
    public void setType(ProjectType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public Integer getAuthorId() { return authorId; }
    public void setAuthorId(Integer authorId) { this.authorId = authorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getHighlighted() { return highlighted; }
    public void setHighlighted(Boolean highlighted) { this.highlighted = highlighted; }

    public Boolean getIsOfficial() { return isOfficial; }
    public void setIsOfficial(Boolean isOfficial) { this.isOfficial = isOfficial; }

    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getExpectedEndDate() { return expectedEndDate; }
    public void setExpectedEndDate(LocalDate expectedEndDate) { this.expectedEndDate = expectedEndDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getFinancialAnalysis() { return financialAnalysis; }
    public void setFinancialAnalysis(String financialAnalysis) { this.financialAnalysis = financialAnalysis; }

    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

    public BigDecimal getApprovedBudget() { return approvedBudget; }
    public void setApprovedBudget(BigDecimal approvedBudget) { this.approvedBudget = approvedBudget; }
}
