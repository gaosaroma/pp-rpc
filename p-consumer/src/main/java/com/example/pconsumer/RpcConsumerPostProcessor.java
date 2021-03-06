package com.example.pconsumer;

import com.example.pcommon.annotation.PReference;
import com.example.premoting.proxy.RpcReferenceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class RpcConsumerPostProcessor implements ApplicationContextAware,BeanClassLoaderAware,BeanFactoryPostProcessor {
    private ApplicationContext context;
    private ClassLoader classLoader;
    private final Map<String, BeanDefinition> rpcRefBeanDefinitions = new LinkedHashMap<>();

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {

        this.classLoader = classLoader;


    }
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {

            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);

            String beanClassName = beanDefinition.getBeanClassName();

            if (beanClassName != null) {

                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);

                ReflectionUtils.doWithFields(clazz, this::parseRpcReference);

            }

        }

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

        this.rpcRefBeanDefinitions.forEach((beanName, beanDefinition) -> {

            if (this.context.containsBean(beanName)) {

                throw new IllegalArgumentException("spring context already has a bean named " + beanName);

            }

            registry.registerBeanDefinition(beanName, rpcRefBeanDefinitions.get(beanName));

            log.info("registered RpcReferenceBean {} success.", beanName);

        });

    }

    private void parseRpcReference(Field field){
        PReference annotation = AnnotationUtils.getAnnotation(field, PReference.class);

        if (annotation != null) {

            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcReferenceBean.class);

            builder.setInitMethodName("init");

            builder.addPropertyValue("interfaceClass", field.getType());

            builder.addPropertyValue("serviceVersion", annotation.serviceVersion());

            builder.addPropertyValue("registryType", annotation.registryType());

            builder.addPropertyValue("registryAddr", annotation.registryAddress());

            builder.addPropertyValue("remoteType",annotation.remoteType());


            builder.addPropertyValue("serialType",annotation.serialType());

            builder.addPropertyValue("timeout", annotation.timeout());

            builder.addPropertyValue("loadBalanceType",annotation.loadBalanceType());

            builder.addPropertyValue("asyncType",annotation.asyncType());

            builder.addPropertyValue("failType",annotation.failType());

            BeanDefinition beanDefinition = builder.getBeanDefinition();

            rpcRefBeanDefinitions.put(field.getName(), beanDefinition);

        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
