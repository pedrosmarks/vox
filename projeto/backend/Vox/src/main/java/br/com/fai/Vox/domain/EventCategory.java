package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Categoria de evento. Tabela dedicada (separada da {@link Category} genérica
 * usada por projetos/ocorrências).
 */
@Getter
@Setter
public class EventCategory {

    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EventCategory() {}
}
