package br.com.fai.Vox.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateEventDto {

    @NotBlank(message = "Título é obrigatório")
    private String title;

    private String description;

    @NotNull(message = "Categoria é obrigatória")
    private Integer categoryId;

    @DecimalMin(value = "0.0", message = "Valor não pode ser negativo")
    private BigDecimal price;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String neighborhood;
    private String street;
    private String number;

    @NotNull(message = "Latitude é obrigatória")
    private BigDecimal latitude;

    @NotNull(message = "Longitude é obrigatória")
    private BigDecimal longitude;

    private Integer municipalityId;
    private Integer authorId;

    private MultipartFile file;
}
