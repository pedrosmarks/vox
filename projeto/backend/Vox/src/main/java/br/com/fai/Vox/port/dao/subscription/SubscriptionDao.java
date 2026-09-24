package br.com.fai.Vox.port.dao.subscription;
import br.com.fai.Vox.domain.enums.SubscriptionTypeEnum;

import br.com.fai.Vox.domain.Subscription;

import java.util.List;

public interface SubscriptionDao {
    void subscribe(int userId, SubscriptionTypeEnum type, Integer targetId);
    void unsubscribe(int userId, SubscriptionTypeEnum type, Integer targetId);
    List<Subscription> findByUserId(int userId);
    List<Subscription> findByTypeAndTargetId(SubscriptionTypeEnum type, Integer targetId);
    List<Subscription> findByType(SubscriptionTypeEnum type);
    boolean exists(int userId, SubscriptionTypeEnum type, Integer targetId);
}
