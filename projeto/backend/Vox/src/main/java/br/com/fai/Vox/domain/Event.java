package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class Event {

    private Integer id;
    private String title;
    private String description;
    private Integer categoryId;
    private BigDecimal price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private Integer municipalityId;
    private Integer authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<EventImage> images;

    public Event() {}
}
