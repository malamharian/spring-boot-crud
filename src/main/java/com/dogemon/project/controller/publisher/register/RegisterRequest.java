package com.dogemon.project.controller.publisher.register;

import com.dogemon.project.controller.publisher.PublisherAPIRequest;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Value
@NoArgsConstructor(force = true)
@SuperBuilder
public class RegisterRequest extends PublisherAPIRequest {
    @NotNull
    String topic;
}
