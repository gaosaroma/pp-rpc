package com.example.pregistry;

import com.example.pcommon.RpcServiceUtil;
import com.example.pcommon.ServiceInfo;
import com.example.pregistry.loadbalancer.ServiceLoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZookeeperRegistryService implements RegistryService {
    public static final int BASE_SLEEP_TIME_MS = 1000;
    public static final int MAX_RETRIES = 3;
    public static final String BASE_PATH = "/pp_rpc";

    private final ServiceDiscovery<ServiceInfo> serviceDiscovery;
    private ServiceLoadBalancer<ServiceInstance<ServiceInfo>> loadBalancer;
    private final static ConcurrentHashMap<String, ServiceCache<ServiceInfo>> cache = new ConcurrentHashMap<String, ServiceCache<ServiceInfo>>();


    public ZookeeperRegistryService(String registryAddr, ServiceLoadBalancer<ServiceInstance<ServiceInfo>> loadBalancer) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(registryAddr, new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
        client.start();
        JsonInstanceSerializer<ServiceInfo> serializer = new JsonInstanceSerializer<>(ServiceInfo.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceInfo.class)
                .client(client)
                .serializer(serializer)
                .basePath(BASE_PATH)
                .build();
        this.serviceDiscovery.start();
        this.loadBalancer = loadBalancer;
    }

    public ZookeeperRegistryService(String registryAddr) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(registryAddr, new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
        client.start();
        JsonInstanceSerializer<ServiceInfo> serializer = new JsonInstanceSerializer<>(ServiceInfo.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceInfo.class)
                .client(client)
                .serializer(serializer)
                .basePath(BASE_PATH)
                .build();
        this.serviceDiscovery.start();
    }

    @Override
    public void register(ServiceInfo serviceInfo) throws Exception {

        ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance
                .<ServiceInfo>builder()
                .name(RpcServiceUtil.buildServiceKey(serviceInfo.getName(), serviceInfo.getVersion()))
                .address(serviceInfo.getAddr())
                .port(serviceInfo.getPort())
                .payload(serviceInfo)
                .build();
        serviceDiscovery.registerService(serviceInstance);
        log.info("register {}:{} {} v{} success", serviceInfo.getAddr(), serviceInfo.getPort(), serviceInfo.getName(), serviceInfo.getVersion());
    }

    @Override
    public void unRegister(ServiceInfo serviceInfo) throws Exception {
        ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance
                .<ServiceInfo>builder()
                .name(RpcServiceUtil.buildServiceKey(serviceInfo.getName(), serviceInfo.getVersion()))
                .address(serviceInfo.getAddr())
                .port(serviceInfo.getPort())
                .payload(serviceInfo)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public ServiceInfo discovery(String serviceKey, int invokerHashCode) throws Exception {
        List<ServiceInstance<ServiceInfo>> serviceInstances;

        ServiceCache<ServiceInfo> serviceCache = getServiceCache(serviceKey);
        serviceInstances = serviceCache.getInstances();
        // 注意：不保证新鲜度。这只是最后一个已知的实例列表。但是，该列表是通过 ZooKeeper watcher 更新的，因此它应该在一两秒的窗口内是新鲜的。
        ServiceInstance<ServiceInfo> instance = loadBalancer.select(serviceInstances, invokerHashCode);
        if (instance != null) {
            return instance.getPayload();
        }
        return null;
    }

    @Override
    public void destroy() throws IOException {
        serviceDiscovery.close();
    }

    private ServiceCache<ServiceInfo> getServiceCache(String serviceKey) throws Exception {
        log.info("query for serviceKey "+serviceKey);
        ServiceCache<ServiceInfo> cache = ZookeeperRegistryService.cache.get(serviceKey);
        if (cache == null) {
            final ServiceCache<ServiceInfo> newCache = serviceDiscovery.serviceCacheBuilder().name(serviceKey).build();
            newCache.start();
            ZookeeperRegistryService.cache.putIfAbsent(serviceKey, newCache);
            log.info("put in cache");
            return newCache;
        }
        log.info("get in cache");
        return cache;
    }
}
