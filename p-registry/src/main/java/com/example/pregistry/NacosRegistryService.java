package com.example.pregistry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.pcommon.RpcServiceUtil;
import com.example.pcommon.ServiceInfo;

import java.util.HashMap;
import java.util.Map;

public class NacosRegistryService implements RegistryService {
    private final NamingService naming;

    public NacosRegistryService(String registryAddr) throws NacosException {
        naming = NamingFactory.createNamingService(registryAddr);
    }

    @Override
    public void register(ServiceInfo serviceInfo) throws Exception {
        Instance instance = new Instance();

        instance.setIp(serviceInfo.getAddr());
        instance.setPort(serviceInfo.getPort());
        Map<String, String> instanceMeta = new HashMap<>();
        instanceMeta.put("name", serviceInfo.getName());
        instanceMeta.put("version", serviceInfo.getVersion());
        instanceMeta.put("addr", serviceInfo.getAddr());
        instanceMeta.put("port", String.valueOf(serviceInfo.getPort()));

        instance.setMetadata(instanceMeta);
        String key = RpcServiceUtil.buildServiceKey(serviceInfo.getName(), serviceInfo.getVersion());
        naming.registerInstance(key, instance);
    }

    @Override
    public void unRegister(ServiceInfo serviceInfo) throws Exception {
        String key = RpcServiceUtil.buildServiceKey(serviceInfo.getName(), serviceInfo.getVersion());
        naming.deregisterInstance(key, serviceInfo.getAddr(), serviceInfo.getPort());
    }

    @Override
    public ServiceInfo discovery(String serviceName, int invokerHashCode) throws Exception {
        Instance instance = naming.selectOneHealthyInstance(serviceName);
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setAddr(instance.getMetadata().get("addr"));
        serviceInfo.setName(instance.getMetadata().get("name"));
        serviceInfo.setVersion(instance.getMetadata().get("version"));
        serviceInfo.setPort(Integer.parseInt(instance.getMetadata().get("port")));
        return serviceInfo;
    }

    @Override
    public void destroy() throws Exception {
        naming.shutDown();
    }
}
