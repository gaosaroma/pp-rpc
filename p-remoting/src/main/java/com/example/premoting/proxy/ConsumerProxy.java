package com.example.premoting.proxy;

import com.example.pcommon.*;
import com.example.premoting.handler.PRequestHolder;
import com.example.premoting.remote.ShortConnectionClient;
import com.example.premoting.remote.Client;
import com.example.premoting.remote.LongConnectionClient;
import com.example.pprotocol.protocol.*;
import com.example.pregistry.RegistryService;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Slf4j
public class ConsumerProxy implements InvocationHandler {
    private final String serviceVersion;
    private final long timeout;
    private final RegistryService registryService;
    private final SerializationType serialType;
    private final AsyncType asyncType;
    private final FailType failType;
    private final Client client;

    public ConsumerProxy(String serviceVersion, long timeout, RegistryService registryService, RemoteType remoteType, SerializationType serialType, AsyncType asyncType, FailType failType) {
        this.registryService = registryService;
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.asyncType = asyncType;
        this.failType = failType;
        this.serialType = serialType;

        if (remoteType == RemoteType.SHORT) {
            this.client = ShortConnectionClient.getInstance();
        } else {
            this.client = LongConnectionClient.getInstance();
        }
    }

    private ServiceInfo lookup(String className, String serviceVersion, Object[] params, RegistryService registryService, int offset) throws Exception {
        String serviceKey = RpcServiceUtil.buildServiceKey(className, serviceVersion);
        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();
        return registryService.discovery(serviceKey, invokerHashCode + offset);

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        PProtocol<PRequest> protocol = new PProtocol<>();
        MsgHeader header = new MsgHeader();
        long requestId = PRequestHolder.REQUEST_ID_GENERATOR.incrementAndGet();

        header.setVersion(ProtocolConst.VERSION);
        header.setRequestId(requestId);
        header.setSerialization((byte) serialType.getType());
        header.setMsgType((byte) MsgType.REQUEST.getType());
        header.setStatus((byte) 0x1);
        protocol.setMsgHeader(header);

        PRequest request = new PRequest();
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParams(args);
        protocol.setBody(request);


        PFuture<PResponse> future = new PFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
        PRequestHolder.REQUEST_MAP.put(requestId, future);

        Object o = null;
        switch (asyncType) {
            case SYNC:
                switch (failType) {
                    case FAIL_OVER:
                        // fail_over 切换4次provider
                        for (int i = 1; i < 10; i++) {
                            try {
                                ServiceInfo nxtService = lookup(request.getClassName(), request.getServiceVersion(), request.getParams(), this.registryService, i);
                                client.sendRequest(protocol, nxtService);
                                o = future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS).getData();
                                return o;
                            } catch (TimeoutException e) {
                                log.info("timeout request for provider, and fail_over");
                                if (i == 9) {
                                    throw e;
                                }
                            }
                        }
                        break;
                    case FAIL_FAST:
                        try {
                            ServiceInfo serviceInfo = lookup(request.getClassName(), request.getServiceVersion(), request.getParams(), this.registryService, 0);
                            client.sendRequest(protocol, serviceInfo);
                            o = future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS).getData();
                            return o;
                        } catch (TimeoutException e) {
                            log.info("timeout request for provider, and fail_fast");
                            throw e;
                        }
                }
                break;
            case ASYNC:
                ServiceInfo serviceInfo = lookup(request.getClassName(), request.getServiceVersion(), request.getParams(), this.registryService, 0);
                client.sendRequest(protocol, serviceInfo);
                RpcContext.setContext(future);
                return o;
        }

        return o;
    }
}
