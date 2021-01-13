package com.dogemon.project.controller.publisher.publish;

import com.dogemon.project.controller.publisher.PublisherAPIRequest;
import com.dogemon.project.controller.publisher.publish.validation.MessageSizeConstraint;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Value
@NoArgsConstructor(force = true)
@SuperBuilder
public class PublishRequest extends PublisherAPIRequest {
    @NotNull
    String topic;
    @NotNull
    @MessageSizeConstraint(message = "Message must not be more than 128KB")
    String message;
}
