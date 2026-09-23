package br.com.fai.Vox.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Filtros da listagem pública de eventos. Todos os campos são opcionais;
 * quando nulos, não aplicam restrição. A listagem NÃO é limitada por município
 * — {@code municipalityId} aqui é apenas um filtro opcional.
 */
@Getter
@Setter
public class EventFilterDto {

    /** Filtra por categoria de evento. */
    private Integer categoryId;

    /** Filtra por município de origem do evento (opcional). */
    private Integer municipalityId;

    /** Busca textual em título e descrição (ILIKE). */
    private String search;

    /** Faixa de valor. */
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    /** Quando {@code true}, retorna apenas eventos gratuitos (price nulo ou 0). */
    private Boolean free;

    /** Quando {@code true}, retorna apenas eventos que possuem ao menos uma imagem. */
    private Boolean hasImage;

    /** Período pela data de início do evento. */
    private LocalDateTime startFrom;
    private LocalDateTime startTo;

    /** Paginação. */
    private int page = 0;
    private int size = 10;

    public EventFilterDto() {}
}
