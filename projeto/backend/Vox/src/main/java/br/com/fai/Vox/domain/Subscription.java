package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Subscription {

    private Integer id;
    private Integer userId;
    private SubscriptionType type;
    private Integer projectId;
    private Integer issueId;
    private Integer categoryId;
    private Integer councilorId;
    private LocalDateTime createdAt;

    public enum SubscriptionType {
        ALL_PROJECTS,
        ALL_ISSUES,
        PROJECT,
        ISSUE,
        CATEGORY,
        COUNCILOR
    }

    public Subscription() {}
}
