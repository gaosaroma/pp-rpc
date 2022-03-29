package com.example.pcommon.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component // 创建bean
public @interface PService {
    Class<?> serviceInterface() default Object.class;
    String serviceVersion() default "1.0";
}
