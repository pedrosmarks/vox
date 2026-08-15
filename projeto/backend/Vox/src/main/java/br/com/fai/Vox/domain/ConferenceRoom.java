package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ConferenceRoom {

    private Integer id;
    private String name;
    private String description;
    private Integer moderatorId;
    private Integer municipalityId;
    private RoomStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum RoomStatus {
        OPEN,
        CLOSED
    }

    public ConferenceRoom() {}
}
