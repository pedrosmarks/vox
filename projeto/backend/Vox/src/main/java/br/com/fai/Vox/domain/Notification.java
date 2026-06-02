package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Notification {

    private Integer id;
    private Integer userId;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public enum NotificationType {
        PROJECT_CREATED,
        PROJECT_UPDATED,
        PROJECT_STATUS_CHANGED,
        ISSUE_CREATED,
        ISSUE_UPDATED,
        ISSUE_STATUS_CHANGED,
        PROJECT_TAGGED,
        ISSUE_TAGGED
    }

    public Notification() {}
}
