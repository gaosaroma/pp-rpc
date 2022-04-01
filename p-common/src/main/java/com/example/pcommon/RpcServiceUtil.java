package com.example.pcommon;

public class RpcServiceUtil {
    public static String buildServiceKey(String serviceName, String serviceVersion){
        return String.join(":",serviceName,serviceVersion);
    }
}
