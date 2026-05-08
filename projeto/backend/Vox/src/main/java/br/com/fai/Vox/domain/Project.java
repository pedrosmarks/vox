package br.com.fai.Vox.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Project {

    private int id;
    private int municipalityId;
    private ProjectType type;
    private String title;
    private String description;
    private ProjectStatus status;
    private int authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean highlighted;
    private Boolean isOfficial;
    private String locationName;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDate endDate;
    private BigDecimal budget;

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

    // Construtor padrão
    public Project() {
    }

    public Project(Integer id, Integer municipalityId, ProjectType type, String title, String description, ProjectStatus status, Integer authorId, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean highlighted, String locationName, Boolean isOfficial, String address, BigDecimal latitude, BigDecimal longitude, LocalDate startDate, LocalDate expectedEndDate, LocalDate endDate, BigDecimal budget) {
        this.id = id;
        this.municipalityId = municipalityId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.status = status;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.highlighted = highlighted;
        this.locationName = locationName;
        this.isOfficial = isOfficial;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startDate = startDate;
        this.expectedEndDate = expectedEndDate;
        this.endDate = endDate;
        this.budget = budget;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getMunicipalityId() { return municipalityId; }
    public void setMunicipalityId(Integer municipalityId) { this.municipalityId = municipalityId; }

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

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

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

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
}
