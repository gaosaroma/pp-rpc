package com.example.premoting.proxy;

import com.example.pcommon.*;
import com.example.pregistry.RegistryFactory;
import com.example.pregistry.RegistryService;
import com.example.pregistry.RegistryType;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

// 实现自定义的bean
// FactoryBean 是一种特种的工厂 Bean，通过 getObject() 方法返回对象Bean，而并不是 FactoryBean 本身。
public class RpcReferenceBean implements FactoryBean<Object> {
    private String serviceVersion;

    private String registryType;
    private AsyncType asyncType;
    private FailType failType;
    private String registryAddr;

    private RemoteType remoteType;

    private LoadBalanceType loadBalanceType;

    private SerializationType serialType;

    private long timeout;

    private Class<?> interfaceClass;

    private Object object;

    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    public void init() throws Exception {
        RegistryService registryService = RegistryFactory.getInstance(this.registryAddr, RegistryType.valueOf(this.registryType), this.loadBalanceType);

        this.object = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ConsumerProxy(serviceVersion, timeout, registryService, remoteType, serialType, asyncType,failType));
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setServiceVersion(String serviceVersion) {

        this.serviceVersion = serviceVersion;

    }

    public void setRegistryType(String registryType) {

        this.registryType = registryType;

    }

    public void setRegistryAddr(String registryAddr) {

        this.registryAddr = registryAddr;

    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;

    }

    public void setRemoteType(RemoteType remoteType) {
        this.remoteType = remoteType;
    }

    public void setSerialType(SerializationType serialType) {
        this.serialType = serialType;
    }

    public void setLoadBalanceType(LoadBalanceType loadBalanceType) {
        this.loadBalanceType = loadBalanceType;
    }

    public void setAsyncType(AsyncType asyncType) {
        this.asyncType = asyncType;
    }

    public void setFailType(FailType failType) {
        this.failType = failType;
    }
}
