package com.example.pregistry.loadbalancer;


import com.example.pcommon.ServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
@Slf4j
public class ConsistentHashLoadBalancer implements ServiceLoadBalancer<ServiceInstance<ServiceInfo>>{
    private final static int VIRTUAL_NODE_SIZE = 5;
    private final static String VIRTUAL_NODE_JOIN = "#";

    @Override
    public ServiceInstance<ServiceInfo> select(List<ServiceInstance<ServiceInfo>> servers, int invokerHashCode) {
        if(servers.size()==0) return null;

        TreeMap<Integer,ServiceInstance<ServiceInfo>> ring = generateConsistentHashRing(servers);
        return allocate(ring, invokerHashCode);
    }

    private ServiceInstance<ServiceInfo> allocate(TreeMap<Integer, ServiceInstance<ServiceInfo>> ring, int invokerHashCode) {
        Map.Entry<Integer, ServiceInstance<ServiceInfo>> entry = ring.ceilingEntry(invokerHashCode);
        if(null==entry){
            entry=ring.firstEntry();
        }
        return entry.getValue();
    }

    private TreeMap<Integer,ServiceInstance<ServiceInfo>> generateConsistentHashRing(List<ServiceInstance<ServiceInfo>> servers){
        TreeMap<Integer,ServiceInstance<ServiceInfo>> ring = new TreeMap<>();
        for (ServiceInstance<ServiceInfo> instance:servers) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                ring.put((buildServiceInstanceKey(instance) + VIRTUAL_NODE_JOIN + i).hashCode(), instance);
            }
        }
//        log.info("ring "+ring.size());
        return ring;
    }

    private String buildServiceInstanceKey(ServiceInstance<ServiceInfo> instance) {
        return String.join(":",instance.getAddress(),String.valueOf(instance.getPort()));
    }
}
