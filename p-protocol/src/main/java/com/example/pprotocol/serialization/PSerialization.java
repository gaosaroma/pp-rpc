package com.example.pprotocol.serialization;

import java.io.IOException;

public interface PSerialization {
    public <T> byte[] serialize(T obj) throws IOException;
    <T> T deserialize(byte[] data,Class<T> cls) throws IOException;
}
