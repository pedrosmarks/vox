package br.com.fai.Vox.port.service.subscription;
import br.com.fai.Vox.domain.enums.SubscriptionTypeEnum;

import br.com.fai.Vox.domain.Subscription;

import java.util.List;

public interface SubscriptionService {
    void subscribe(int userId, SubscriptionTypeEnum type, Integer targetId);
    void unsubscribe(int userId, SubscriptionTypeEnum type, Integer targetId);
    List<Subscription> findByUserId(int userId);
    List<Integer> findSubscriberUserIds(SubscriptionTypeEnum type, Integer targetId);
}
