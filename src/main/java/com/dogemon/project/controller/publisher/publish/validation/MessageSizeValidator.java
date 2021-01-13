package com.dogemon.project.controller.publisher.publish.validation;

import com.google.common.base.Charsets;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MessageSizeValidator implements ConstraintValidator<MessageSizeConstraint, String> {
    int byteLimit = 1024 * 128; // 128 KB
    @Override
    public boolean isValid(String message, ConstraintValidatorContext context) {
        // validating using UTF_8 charset
        return message == null || message.getBytes(Charsets.UTF_8).length <= byteLimit;
    }
}
