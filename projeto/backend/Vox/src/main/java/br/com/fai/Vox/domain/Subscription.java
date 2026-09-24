package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import br.com.fai.Vox.domain.enums.SubscriptionTypeEnum;

@Getter
@Setter
public class Subscription {

    private Integer id;
    private Integer userId;
    private SubscriptionTypeEnum type;
    private Integer projectId;
    private Integer issueId;
    private Integer categoryId;
    private Integer councilorId;
    private LocalDateTime createdAt;

    public Subscription() {}
}
