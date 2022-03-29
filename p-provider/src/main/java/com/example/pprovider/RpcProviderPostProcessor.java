package com.example.pprovider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

@Slf4j

public class RpcProviderPostProcessor implements BeanPostProcessor {

    @Autowired
    RpcProviderAutoConfig rpcProviderAutoConfig;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//        log.info("hello");
//        log.info(rpcProviderAutoConfig.rpcProperties.getRegistryType());
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
