package com.example.pregistry.loadbalancer;

import com.example.pcommon.ServiceInfo;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.List;

public interface ServiceLoadBalancer<T> {
    ServiceInstance<ServiceInfo> select(List<ServiceInstance<ServiceInfo>> servers, int requestHashCode);
}
