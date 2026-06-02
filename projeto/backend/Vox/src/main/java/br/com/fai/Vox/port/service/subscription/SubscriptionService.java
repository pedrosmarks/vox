package br.com.fai.Vox.port.service.subscription;

import br.com.fai.Vox.domain.Subscription;

import java.util.List;

public interface SubscriptionService {
    void subscribe(int userId, Subscription.SubscriptionType type, Integer targetId);
    void unsubscribe(int userId, Subscription.SubscriptionType type, Integer targetId);
    List<Subscription> findByUserId(int userId);
    List<Integer> findSubscriberUserIds(Subscription.SubscriptionType type, Integer targetId);
}
