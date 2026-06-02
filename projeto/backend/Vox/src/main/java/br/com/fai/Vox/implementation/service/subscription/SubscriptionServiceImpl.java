package br.com.fai.Vox.implementation.service.subscription;

import br.com.fai.Vox.domain.Subscription;
import br.com.fai.Vox.port.dao.subscription.SubscriptionDao;
import br.com.fai.Vox.port.service.subscription.SubscriptionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger logger = Logger.getLogger(SubscriptionServiceImpl.class.getName());

    private final SubscriptionDao subscriptionDao;

    public SubscriptionServiceImpl(SubscriptionDao subscriptionDao) {
        this.subscriptionDao = subscriptionDao;
    }

    @Override
    public void subscribe(int userId, Subscription.SubscriptionType type, Integer targetId) {
        if (userId <= 0 || type == null) return;
        subscriptionDao.subscribe(userId, type, targetId);
        logger.log(Level.INFO, "Subscription: userId=" + userId + " type=" + type + " targetId=" + targetId);
    }

    @Override
    public void unsubscribe(int userId, Subscription.SubscriptionType type, Integer targetId) {
        if (userId <= 0 || type == null) return;
        subscriptionDao.unsubscribe(userId, type, targetId);
    }

    @Override
    public List<Subscription> findByUserId(int userId) {
        if (userId <= 0) return List.of();
        return subscriptionDao.findByUserId(userId);
    }

    @Override
    public List<Integer> findSubscriberUserIds(Subscription.SubscriptionType type, Integer targetId) {
        if (type == null) return List.of();
        List<Subscription> subs = targetId != null
                ? subscriptionDao.findByTypeAndTargetId(type, targetId)
                : subscriptionDao.findByType(type);
        return subs.stream().map(Subscription::getUserId).distinct().toList();
    }
}
