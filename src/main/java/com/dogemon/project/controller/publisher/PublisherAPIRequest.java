package com.dogemon.project.controller.publisher;

import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@Value
@NonFinal
@SuperBuilder
@NoArgsConstructor(force = true)
public abstract class PublisherAPIRequest {
    // Assuming security is already handled by the infra
    // Using publisher id directly instead of token assuming we can trust it
    @NotNull
    String publisherId;
}
