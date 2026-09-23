package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento publicado na plataforma. Visível por qualquer usuário, de qualquer
 * município (sem restrição de visualização). Apenas MODERATOR/ADMINISTRATOR
 * podem criar/editar/remover.
 */
@Getter
@Setter
public class Event {

    private Integer id;
    private String title;
    private String description;
    private Integer categoryId;
    /** Valor do evento; {@code null} = gratuito / sem valor definido. */
    private BigDecimal price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    /** Município de origem (apenas para filtro; não restringe a visualização). */
    private Integer municipalityId;
    private Integer authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Imagens associadas (carregadas sob demanda; opcional). */
    private List<EventImage> images;

    public Event() {}
}
