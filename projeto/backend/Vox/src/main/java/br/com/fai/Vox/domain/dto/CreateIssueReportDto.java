package br.com.fai.Vox.domain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateIssueReportDto {

    private Integer municipalityId;   // preenchido pelo backend via token
    @NotNull(message = "Categoria é obrigatória")
    private Integer categoryId;
    private Integer authorId;         // preenchido pelo backend via token
    private Integer councilorId;
    @NotBlank(message = "Título é obrigatório")
    private String title;
    @NotBlank(message = "Descrição é obrigatória")
    private String description;
    private String neighborhood;
    private String street;
    private String number;
    @NotNull(message = "Latitude é obrigatória")
    @DecimalMin(value = "-90.0", message = "Latitude inválida")
    @DecimalMax(value = "90.0", message = "Latitude inválida")
    private BigDecimal latitude;
    @NotNull(message = "Longitude é obrigatória")
    @DecimalMin(value = "-180.0", message = "Longitude inválida")
    @DecimalMax(value = "180.0", message = "Longitude inválida")
    private BigDecimal longitude;
    private MultipartFile file;
}
