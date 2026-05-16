package br.com.fai.Vox.domain.dto;

import br.com.fai.Vox.domain.Project;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateProjectDto {
    // Campos do objeto de projeto
    private Integer id;
    private Integer municipalityId;
    private Integer categoryId;
    private Project.ProjectType type;
    private String title;
    private String description;
    private Project.ProjectStatus status;
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

    private MultipartFile file;

    public CreateProjectDto() {
    }

//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public Integer getMunicipalityId() {
//        return municipalityId;
//    }
//
//    public void setMunicipalityId(Integer municipalityId) {
//        this.municipalityId = municipalityId;
//    }
//
//    public Integer getCategoryId() {
//        return categoryId;
//    }
//
//    public void setCategoryId(Integer categoryId) {
//        this.categoryId = categoryId;
//    }
//
//    public Project.ProjectType getType() {
//        return type;
//    }
//
//    public void setType(Project.ProjectType type) {
//        this.type = type;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public Project.ProjectStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(Project.ProjectStatus status) {
//        this.status = status;
//    }
//
//    public Integer getAuthorId() {
//        return authorId;
//    }
//
//    public void setAuthorId(Integer authorId) {
//        this.authorId = authorId;
//    }
//
//    public LocalDateTime getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public LocalDateTime getUpdatedAt() {
//        return updatedAt;
//    }
//
//    public void setUpdatedAt(LocalDateTime updatedAt) {
//        this.updatedAt = updatedAt;
//    }
//
//    public Boolean getHighlighted() {
//        return highlighted;
//    }
//
//    public void setHighlighted(Boolean highlighted) {
//        this.highlighted = highlighted;
//    }
//
//    public Boolean getOfficial() {
//        return isOfficial;
//    }
//
//    public void setOfficial(Boolean official) {
//        isOfficial = official;
//    }
//
//    public String getNeighborhood() {
//        return neighborhood;
//    }
//
//    public void setNeighborhood(String neighborhood) {
//        this.neighborhood = neighborhood;
//    }
//
//    public String getStreet() {
//        return street;
//    }
//
//    public void setStreet(String street) {
//        this.street = street;
//    }
//
//    public String getNumber() {
//        return number;
//    }
//
//    public void setNumber(String number) {
//        this.number = number;
//    }
//
//    public BigDecimal getLatitude() {
//        return latitude;
//    }
//
//    public void setLatitude(BigDecimal latitude) {
//        this.latitude = latitude;
//    }
//
//    public BigDecimal getLongitude() {
//        return longitude;
//    }
//
//    public void setLongitude(BigDecimal longitude) {
//        this.longitude = longitude;
//    }
//
//    public LocalDate getStartDate() {
//        return startDate;
//    }
//
//    public void setStartDate(LocalDate startDate) {
//        this.startDate = startDate;
//    }
//
//    public LocalDate getExpectedEndDate() {
//        return expectedEndDate;
//    }
//
//    public void setExpectedEndDate(LocalDate expectedEndDate) {
//        this.expectedEndDate = expectedEndDate;
//    }
//
//    public LocalDate getEndDate() {
//        return endDate;
//    }
//
//    public void setEndDate(LocalDate endDate) {
//        this.endDate = endDate;
//    }
//
//    public String getFinancialAnalysis() {
//        return financialAnalysis;
//    }
//
//    public void setFinancialAnalysis(String financialAnalysis) {
//        this.financialAnalysis = financialAnalysis;
//    }
//
//    public BigDecimal getEstimatedCost() {
//        return estimatedCost;
//    }
//
//    public void setEstimatedCost(BigDecimal estimatedCost) {
//        this.estimatedCost = estimatedCost;
//    }
//
//    public BigDecimal getApprovedBudget() {
//        return approvedBudget;
//    }
//
//    public void setApprovedBudget(BigDecimal approvedBudget) {
//        this.approvedBudget = approvedBudget;
//    }
//
//    public MultipartFile getFile() {
//        return file;
//    }
//
//    public void setFile(MultipartFile file) {
//        this.file = file;
//    }
}
