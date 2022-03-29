package com.example.pregistry.loadbalancer;

import com.example.pcommon.ServiceInfo;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RoundRobinLoadBalancer implements ServiceLoadBalancer<ServiceInstance<ServiceInfo>> {
    ConcurrentHashMap<String, Integer> roundRobin = new ConcurrentHashMap<>();

    @Override
    public ServiceInstance<ServiceInfo> select(List<ServiceInstance<ServiceInfo>> servers, int requestHashCode) {
        int n = servers.size();
        if (n != 0) {
            String name = servers.get(0).getName();
            if (!roundRobin.containsKey(name)) {
                roundRobin.put(name, 0);
            }
            int i = roundRobin.get(name);
            ServiceInstance<ServiceInfo> server = servers.get(i % n);
            roundRobin.put(name, i + 1);
            return server;
        }
        return null;
    }
}
