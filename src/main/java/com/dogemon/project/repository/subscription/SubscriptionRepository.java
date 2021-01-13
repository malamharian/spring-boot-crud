package com.dogemon.project.repository.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Subscription.SubscriptionId> {
}
