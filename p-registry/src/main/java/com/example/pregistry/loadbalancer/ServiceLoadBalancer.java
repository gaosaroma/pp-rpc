package com.example.pregistry.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.pcommon.ServiceInfo;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.List;

public interface ServiceLoadBalancer<T> {
    T select(List<T> servers, int requestHashCode);
}
