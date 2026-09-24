package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import br.com.fai.Vox.domain.enums.NotificationTypeEnum;

@Getter
@Setter
public class Notification {

    private Integer id;
    private Integer userId;
    private String title;
    private String message;
    private NotificationTypeEnum type;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public Notification() {}
}
