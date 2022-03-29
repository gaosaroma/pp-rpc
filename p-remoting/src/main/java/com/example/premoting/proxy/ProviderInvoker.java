package com.example.premoting.proxy;

import com.example.pcommon.RpcServiceUtil;
import com.example.pprotocol.protocol.PRequest;
import org.springframework.cglib.reflect.FastClass;
import java.util.Map;

public class ProviderInvoker {
    private final Map<String, Object> rpcServiceMap;
    public ProviderInvoker(Map<String, Object> rpcServiceMap) {
        this.rpcServiceMap = rpcServiceMap;
    }


    public Object handle(PRequest request) throws Throwable {
        String serviceKey = RpcServiceUtil.buildServiceKey(request.getClassName(), request.getServiceVersion());
        Object serviceBean = rpcServiceMap.get(serviceKey);

        if (null == serviceBean) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParams();

        FastClass fastClass = FastClass.create(serviceClass);
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }
}
