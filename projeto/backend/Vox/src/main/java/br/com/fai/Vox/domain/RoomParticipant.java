package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import br.com.fai.Vox.domain.enums.ParticipantStatusEnum;
import br.com.fai.Vox.domain.enums.SpeechRequestStatusEnum;

@Getter
@Setter
public class RoomParticipant {

    private Integer id;
    private Integer roomId;
    private Integer userId;
    private ParticipantStatusEnum status;
    private SpeechRequestStatusEnum speechRequestStatus;
    private Boolean canPublishAudio;
    private Boolean canPublishVideo;
    private LocalDateTime requestedAt;
    private LocalDateTime decidedAt;
    private LocalDateTime speechRequestedAt;
    private LocalDateTime speechDecidedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RoomParticipant() {}
}
