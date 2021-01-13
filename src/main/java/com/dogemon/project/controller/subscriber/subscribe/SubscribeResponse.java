package com.dogemon.project.controller.subscriber.subscribe;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class SubscribeResponse {
    private static final String SUCCESS = "Subscription successful";
    private static final String TOPIC_NOT_FOUND = "Topic not found";
    private static final String ALREADY_SUBSCRIBED = "Already subscribed";

    @NotNull
    boolean isSuccessful;
    @NotNull
    String message;

    public static SubscribeResponse getSuccessResponse() {
        return new SubscribeResponse(true, SUCCESS);
    }

    public static SubscribeResponse getTopicNotFoundResponse() {
        return new SubscribeResponse(false, TOPIC_NOT_FOUND);
    }

    public static SubscribeResponse getAlreadySubscribedResponse() {
        return new SubscribeResponse(false, ALREADY_SUBSCRIBED);
    }
}
