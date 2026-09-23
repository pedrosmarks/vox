package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventImage {

    private Integer id;
    private Integer eventId;
    private String url;
    private LocalDateTime createdAt;

    public EventImage() {}
}
