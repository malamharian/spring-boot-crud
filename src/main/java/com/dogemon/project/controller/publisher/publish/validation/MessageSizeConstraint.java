package com.dogemon.project.controller.publisher.publish.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {MessageSizeValidator.class})
public @interface MessageSizeConstraint {

    String message() default "Message too large";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
