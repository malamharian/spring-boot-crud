package com.dogemon.project.controller.subscriber.message.ack;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class AckMessageResponse {
    @NotNull
    boolean successful;
    @NotNull
    String message;

    public static AckMessageResponse getSuccessResponse() {
        return new AckMessageResponse(
                true,
                "Ack successful"
        );
    }

    public static AckMessageResponse getMessageNotFoundResponse() {
        return new AckMessageResponse(
                false,
                "Message not found"
        );
    }
}
