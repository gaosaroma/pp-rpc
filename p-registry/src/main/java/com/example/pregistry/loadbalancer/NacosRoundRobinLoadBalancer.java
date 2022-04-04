package com.example.pregistry.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.pcommon.ServiceInfo;
import org.apache.curator.x.discovery.ServiceInstance;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NacosRoundRobinLoadBalancer implements ServiceLoadBalancer<Instance> {
    ConcurrentHashMap<String, Integer> roundRobin = new ConcurrentHashMap<>();

    @Override
    public Instance select(List<Instance> servers, int requestHashCode) {
        int n = servers.size();
        if (n != 0) {
            String name = servers.get(0).getServiceName();
            if (!roundRobin.containsKey(name)) {
                roundRobin.put(name, 0);
            }
            int i = roundRobin.get(name);
            Instance server = servers.get(i % n);
            roundRobin.put(name, i + 1);
            return server;
        }
        return null;
    }
}
