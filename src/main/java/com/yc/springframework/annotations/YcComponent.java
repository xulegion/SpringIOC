package com.yc.springframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {TYPE})
public @interface YcComponent {
    String value() default "";
}
