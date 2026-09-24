package br.com.fai.Vox.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class EventFilterDto {

    private Integer categoryId;

    private Integer municipalityId;

    private String search;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private Boolean free;

    private Boolean hasImage;

    private LocalDateTime startFrom;
    private LocalDateTime startTo;

    private int page = 0;
    private int size = 10;

    public EventFilterDto() {}
}
