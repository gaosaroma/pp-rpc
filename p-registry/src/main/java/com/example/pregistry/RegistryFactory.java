package com.example.pregistry;

import com.example.pcommon.LoadBalanceType;
import com.example.pregistry.loadbalancer.ConsistentHashLoadBalancer;
import com.example.pregistry.loadbalancer.RoundRobinLoadBalancer;

public class RegistryFactory {
    private static volatile RegistryService registryService;

    public static RegistryService getInstance(String registryAddr,RegistryType registryType, LoadBalanceType loadBalanceType) throws Exception {
        if (null == registryService) {
            synchronized (RegistryFactory.class) {
                if (null == registryService) {
                    switch (registryType){
                        case ZOOKEEPER:
                            switch (loadBalanceType) {
                                case CONSISTENT_HASH:
                                    registryService = new ZookeeperRegistryService(registryAddr, new ConsistentHashLoadBalancer());
                                    break;
                                case ROUND_ROBIN:
                                    registryService = new ZookeeperRegistryService(registryAddr, new RoundRobinLoadBalancer());
                                    break;
                            }
                            break;
                        case NACOS:
                            registryService = new NacosRegistryService(registryAddr);
                            break;
                    }

                }
            }
        }
        return registryService;
    }

    public static RegistryService getInstance(String registryAddr, RegistryType registryType) throws Exception {
        if (null == registryService) {
            synchronized (RegistryFactory.class) {
                if (null == registryService) {
                    switch (registryType){
                        case ZOOKEEPER:
                            registryService = new ZookeeperRegistryService(registryAddr);
                            break;
                        case NACOS:
                            registryService = new NacosRegistryService(registryAddr);
                            break;
                    }

                }
            }
        }
        return registryService;
    }


}
