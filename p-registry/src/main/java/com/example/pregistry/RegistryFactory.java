package com.example.pregistry;

import com.example.pcommon.LoadBalanceType;
import com.example.pregistry.loadbalancer.ConsistentHashLoadBalancer;
import com.example.pregistry.loadbalancer.RoundRobinLoadBalancer;

public class RegistryFactory {
    private static volatile RegistryService registryService;

    public static RegistryService getInstance(String registryAddr, LoadBalanceType loadBalanceType) throws Exception {
        if (null == registryService) {
            synchronized (RegistryFactory.class) {
                if (null == registryService) {
                    switch (loadBalanceType) {
                        case CONSISTENT_HASH:
                            registryService = new ZookeeperRegistryService(registryAddr, new ConsistentHashLoadBalancer());
                            break;
                        case ROUND_ROBIN:
                            registryService = new ZookeeperRegistryService(registryAddr, new RoundRobinLoadBalancer());
                            break;
                    }
                }
            }
        }
        return registryService;
    }

    public static RegistryService getInstance(String registryAddr,RegistryType registryType) throws Exception {
        if (null == registryService) {
            synchronized (RegistryFactory.class) {
                if (null == registryService) {
                    registryService = new ZookeeperRegistryService(registryAddr);

                }
            }
        }
        return registryService;
    }


}
