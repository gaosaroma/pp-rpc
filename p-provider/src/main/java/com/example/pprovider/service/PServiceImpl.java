package com.example.pprovider.service;

import com.example.pcommon.annotation.PService;

import java.util.concurrent.TimeUnit;


@PService(serviceInterface = com.example.pservice.PService.class,serviceVersion = "1.0")
public class PServiceImpl implements com.example.pservice.PService {
    @Override
    public String helloPP(String name) {
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello! "+name;
    }
}
