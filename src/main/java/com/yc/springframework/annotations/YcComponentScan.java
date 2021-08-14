package com.yc.springframework.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {TYPE})
public @interface YcComponentScan {

    String basePackage() default "";
}
