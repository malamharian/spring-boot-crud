package com.dogemon.project.repository.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity
@Value
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@IdClass(Subscription.SubscriptionId.class)
public class Subscription {
    @Id
    String topic;
    @Id
    String subscriberId;

    @Value
    @NoArgsConstructor(force = true)
    @AllArgsConstructor
    public static class SubscriptionId implements Serializable {
        String topic;
        String subscriberId;
    }
}
