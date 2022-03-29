package com.example.pprotocol.protocol;

import lombok.Data;

import java.io.Serializable;

@Data
public class PResponse implements Serializable {
    private Object data; // 请求结果
    private String message; // 错误信息
}
