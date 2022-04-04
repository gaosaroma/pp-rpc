package com.example.pregistry.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.pcommon.ServiceInfo;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NacosConsistentHashLoadBalancer implements ServiceLoadBalancer<Instance> {
    private final static int VIRTUAL_NODE_SIZE = 5;
    private final static String VIRTUAL_NODE_JOIN = "#";


    @Override
    public Instance select(List<Instance> servers, int invokerHashCode) {
        if (servers.size() == 0) return null;

        TreeMap<Integer, Instance> ring = generateConsistentHashRing(servers);
        return allocate(ring, invokerHashCode);
    }

    private Instance allocate(TreeMap<Integer, Instance> ring, int invokerHashCode) {
        Map.Entry<Integer, Instance> entry = ring.ceilingEntry(invokerHashCode);
        if (null == entry) {
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    private TreeMap<Integer, Instance> generateConsistentHashRing(List<Instance> servers) {
        TreeMap<Integer, Instance> ring = new TreeMap<>();
        for (Instance instance : servers) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                ring.put((instance.getServiceName() + VIRTUAL_NODE_JOIN + i).hashCode(), instance);
            }
        }
        return ring;
    }
}
