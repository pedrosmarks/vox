package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RoomParticipant {

    private Integer id;
    private Integer roomId;
    private Integer userId;
    private ParticipantStatus status;
    private SpeechRequestStatus speechRequestStatus;
    private Boolean canPublishAudio;
    private Boolean canPublishVideo;
    private LocalDateTime requestedAt;
    private LocalDateTime decidedAt;
    private LocalDateTime speechRequestedAt;
    private LocalDateTime speechDecidedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ParticipantStatus {
        PENDING,
        APPROVED,
        REJECTED,
        REMOVED
    }

    public enum SpeechRequestStatus {
        NOT_REQUESTED,
        PENDING,
        APPROVED,
        REJECTED
    }

    public RoomParticipant() {}
}
