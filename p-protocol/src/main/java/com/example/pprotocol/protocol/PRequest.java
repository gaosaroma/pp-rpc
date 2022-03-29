package com.example.pprotocol.protocol;

import lombok.Data;

import java.io.Serializable;

@Data
public class PRequest implements Serializable {
    private String serviceVersion; // 版本号
    private String className; // 类名
    private String methodName; //方法名
    private Object[] params;//参数
    private Class<?>[] parameterTypes;//参数类型
}
