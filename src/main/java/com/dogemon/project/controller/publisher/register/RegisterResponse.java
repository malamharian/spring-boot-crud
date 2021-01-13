package com.dogemon.project.controller.publisher.register;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class RegisterResponse {
    private static final String SUCCESS = "Register successful";
    private static final String TOPIC_ALREADY_EXISTS = "Topic already exists";

    @NotNull
    boolean isSuccessful;
    @NotNull
    String message;

    public static RegisterResponse getSuccessResponse() {
        return new RegisterResponse(true, SUCCESS);
    }

    public static RegisterResponse getTopicAlreadyExistsResponse() {
        return new RegisterResponse(true, TOPIC_ALREADY_EXISTS);
    }
}
