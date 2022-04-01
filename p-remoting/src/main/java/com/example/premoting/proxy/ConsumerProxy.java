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

    private final Client client;

    public ConsumerProxy(String serviceVersion, long timeout, RegistryService registryService, RemoteType remoteType, SerializationType serialType, AsyncType asyncType) {
        this.registryService = registryService;
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.asyncType = asyncType;

        this.serialType = serialType;

        if (remoteType == RemoteType.SHORT) {
            this.client = ShortConnectionClient.getInstance();
        } else {
            this.client = LongConnectionClient.getInstance();
        }
    }

    private ServiceInfo lookup(String className, String serviceVersion, Object[] params, RegistryService registryService) throws Exception {
        String serviceKey = RpcServiceUtil.buildServiceKey(className, serviceVersion);
        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();
        return registryService.discovery(serviceKey, invokerHashCode);

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

        ServiceInfo serviceInfo = lookup(request.getClassName(), request.getServiceVersion(), request.getParams(), this.registryService);

        PFuture<PResponse> future = new PFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);

        PRequestHolder.REQUEST_MAP.put(requestId, future);


        client.sendRequest(protocol, serviceInfo);

        Object o = null;
        switch (asyncType) {
            case SYNC:

                try {
                    o = future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS).getData();
                } catch (TimeoutException e){
                    // TODO failover

                };

                break;
            case ASYNC:
                RpcContext.setContext(future);
                break;
        }

        return o;
    }
}
