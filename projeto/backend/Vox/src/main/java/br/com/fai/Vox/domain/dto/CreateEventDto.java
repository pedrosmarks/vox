package br.com.fai.Vox.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dados de criação/edição de um evento. Enviado como {@code multipart/form-data}
 * pois aceita uma imagem opcional no momento da criação (imagens adicionais são
 * enviadas pelo sub-recurso {@code /api/events/{id}/images}).
 */
@Getter
@Setter
public class CreateEventDto {

    @NotBlank(message = "Título é obrigatório")
    private String title;

    private String description;

    @NotNull(message = "Categoria é obrigatória")
    private Integer categoryId;

    /** Valor opcional; ausente ou nulo = evento gratuito. */
    @DecimalMin(value = "0.0", message = "Valor não pode ser negativo")
    private BigDecimal price;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;

    // Preenchidos pelo backend a partir do token (não vêm do cliente).
    private Integer municipalityId;
    private Integer authorId;

    /** Imagem opcional enviada na criação. */
    private MultipartFile file;
}
