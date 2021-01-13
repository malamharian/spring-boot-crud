package com.dogemon.project.controller.subscriber.message.get;

import com.dogemon.project.repository.message.Message;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class GetMessageResponse {
    Long messageId;

    String message;

    @NotNull
    boolean successful;
    @NotNull
    boolean hasMessage;

    String errorMessage;

    public static GetMessageResponse getSuccessResponse(Message message) {
        return new GetMessageResponse(
                message.getId(),
                message.getMessage(),
                true,
                true,
                null
        );
    }

    public static GetMessageResponse getNoMessageAvailableResponse() {
        return new GetMessageResponse(
                null,
                null,
                true,
                false,
                "No message available"
        );
    }

    public static GetMessageResponse getTopicNotFoundResponse() {
        return new GetMessageResponse(
                null,
                null,
                false,
                false,
                "Topic not found"
        );
    }

    public static GetMessageResponse getUnsubscribedResponse() {
        return new GetMessageResponse(
                null,
                null,
                false,
                false,
                "Not subscribed to this topic"
        );
    }
}
