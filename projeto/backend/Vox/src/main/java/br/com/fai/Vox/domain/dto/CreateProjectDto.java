package br.com.fai.Vox.domain.dto;
import br.com.fai.Vox.domain.enums.ProjectNatureEnum;
import br.com.fai.Vox.domain.enums.ProjectStatusEnum;
import br.com.fai.Vox.domain.enums.ProjectTypeEnum;

import br.com.fai.Vox.domain.Project;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateProjectDto {
    private Integer id;
    private Integer municipalityId;
    @NotNull(message = "Categoria é obrigatória")
    private Integer categoryId;
    @NotNull(message = "Tipo é obrigatório")
    private ProjectTypeEnum type;
    @NotNull(message = "Natureza é obrigatória (LAW ou PUBLIC_WORK)")
    private ProjectNatureEnum nature;
    @NotBlank(message = "Título é obrigatório")
    private String title;
    @NotBlank(message = "Descrição é obrigatória")
    private String description;
    private ProjectStatusEnum status;
    private Integer authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean highlighted;
    private Boolean isOfficial;
    private String neighborhood;
    private String street;
    private String number;
    @DecimalMin(value = "-90.0", message = "Latitude inválida")
    @DecimalMax(value = "90.0", message = "Latitude inválida")
    private BigDecimal latitude;
    @DecimalMin(value = "-180.0", message = "Longitude inválida")
    @DecimalMax(value = "180.0", message = "Longitude inválida")
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

}
