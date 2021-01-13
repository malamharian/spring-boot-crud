package com.dogemon.project.controller.subscriber;

import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@Value
@NonFinal
@SuperBuilder
@NoArgsConstructor(force = true)
public abstract class SubscriberAPIRequest {
    // Assuming security is already handled by the infra
    // Using subscriber id directly instead of token assuming we can trust it
    @NotNull
    String subscriberId;
}
