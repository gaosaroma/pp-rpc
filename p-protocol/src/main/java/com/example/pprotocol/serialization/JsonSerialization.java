package com.example.pprotocol.serialization;

import com.alibaba.fastjson.JSON;

import java.io.IOException;


public class JsonSerialization implements PSerialization{
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        if (obj == null) {
            throw new NullPointerException();
        }
        return JSON.toJSONBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> cls) throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        }
        return JSON.parseObject(bytes,cls);
    }
}
