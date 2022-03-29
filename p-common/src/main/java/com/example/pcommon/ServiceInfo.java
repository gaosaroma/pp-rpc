package com.example.pcommon;
import lombok.Data;

@Data
public class ServiceInfo {
    private String name;
    private String version;
    private String addr;
    private int port;
}
