package com.example.pregistry;

import com.example.pcommon.ServiceInfo;

import java.io.IOException;

public interface RegistryService {
    void register(ServiceInfo serviceInfo) throws Exception;

    void unRegister(ServiceInfo serviceInfo) throws Exception;

    ServiceInfo discovery(String serviceName, int invokerHashCode) throws Exception;

    void destroy() throws IOException;
}
