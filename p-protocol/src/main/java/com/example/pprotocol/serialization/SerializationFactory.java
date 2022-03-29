package com.example.pprotocol.serialization;

import com.example.pcommon.SerializationType;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
@Slf4j
public class SerializationFactory {
    private static final HashMap<SerializationType,PSerialization> serializationMap = new HashMap<>();

    public static PSerialization getPSerialization(byte serializationType) {
        SerializationType sType = SerializationType.findByType(serializationType);

        if(!serializationMap.containsKey(sType)){
            synchronized (SerializationFactory.class){
                if(!serializationMap.containsKey(sType)){
                    switch (sType){
                        case PROTOBUF:
                            serializationMap.put(sType,new ProtobufSerialization());
                            log.info("registry ProtobufSerialization() in serializationMap success");
                            break;
                        case HESSIAN:
                            serializationMap.put(sType,new HessianSerialization());
                            log.info("registry HessianSerialization() in serializationMap success");
                            break;
                        case FASTJSON:
                            serializationMap.put(sType,new JsonSerialization());
                            log.info("registry JsonSerialization() in serializationMap success");
                            break;
                        default:
                            throw new IllegalArgumentException("serialization type is illegal, " + serializationType);

                    }
                }
            }
        }

        return  serializationMap.get(sType);


    }
}
