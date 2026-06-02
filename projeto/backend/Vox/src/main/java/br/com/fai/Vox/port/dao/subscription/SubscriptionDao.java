package br.com.fai.Vox.port.dao.subscription;

import br.com.fai.Vox.domain.Subscription;

import java.util.List;

public interface SubscriptionDao {
    void subscribe(int userId, Subscription.SubscriptionType type, Integer targetId);
    void unsubscribe(int userId, Subscription.SubscriptionType type, Integer targetId);
    List<Subscription> findByUserId(int userId);
    List<Subscription> findByTypeAndTargetId(Subscription.SubscriptionType type, Integer targetId);
    List<Subscription> findByType(Subscription.SubscriptionType type);
    boolean exists(int userId, Subscription.SubscriptionType type, Integer targetId);
}
