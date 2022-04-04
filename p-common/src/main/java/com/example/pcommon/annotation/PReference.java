package com.example.pcommon.annotation;

import com.example.pcommon.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Autowired
public @interface PReference {
    String serviceVersion() default "1.0";

    String registryType() default "ZOOKEEPER"; // "NACOS"

    String registryAddress() default "127.0.0.1:2181"; // "127.0.0.1:8848"

    RemoteType remoteType() default RemoteType.LONG;

    AsyncType asyncType() default AsyncType.SYNC;

    FailType failType() default FailType.FAIL_FAST;

    SerializationType serialType() default SerializationType.HESSIAN;

    LoadBalanceType loadBalanceType() default LoadBalanceType.CONSISTENT_HASH;

    long timeout() default 5000;
}
