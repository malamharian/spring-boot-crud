package com.dogemon.project.controller.subscriber.subscribe;

import com.dogemon.project.controller.subscriber.SubscriberAPIRequest;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Value
@NoArgsConstructor(force = true)
@SuperBuilder
public class SubscribeRequest extends SubscriberAPIRequest {
    @NotNull
    String topic;
}
