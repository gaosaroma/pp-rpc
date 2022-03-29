package com.example.pprovider;


import com.example.pcommon.RpcProperties;
import com.example.premoting.remote.Server;
import com.example.pregistry.RegistryFactory;
import com.example.pregistry.RegistryService;
import com.example.pregistry.RegistryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
@Slf4j
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class RpcProviderAutoConfig {

    {
        log.info("Load RpcProviderAutoConfig Class");
    }

    @Resource
    public RpcProperties rpcProperties;

    @Bean
    public Server init() throws Exception {
        log.info("invoke init() of RpcProviderAutoConfig");
        RegistryType type = RegistryType.valueOf(rpcProperties.getRegistryType());
        String registryAddr = rpcProperties.getRegistryAddr();
        RegistryService registryService = RegistryFactory.getInstance(registryAddr,type);

        return new Server(rpcProperties.getServicePort(),registryService);
    }


}
