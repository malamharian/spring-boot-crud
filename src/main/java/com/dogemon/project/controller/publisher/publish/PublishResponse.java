package com.dogemon.project.controller.publisher.publish;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class PublishResponse {
    @NotNull
    boolean isSuccessful;
    @NotNull
    String message;

    public static PublishResponse getUnauthorizedResponse() {
        return new PublishResponse(
                false,
                "Unauthorized to publish message to this topic"
        );
    }

    public static PublishResponse getTopicNotFoundResponse() {
        return new PublishResponse(
                false,
                "Topic not found"
        );
    }

    public static PublishResponse getSuccessResponse() {
        return new PublishResponse(
                true,
                "Message published successfully"
        );
    }
}
