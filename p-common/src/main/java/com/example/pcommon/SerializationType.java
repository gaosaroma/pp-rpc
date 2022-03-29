package com.example.pcommon;

import lombok.Getter;

public enum SerializationType {
    HESSIAN(1),
    PROTOBUF(2),
    FASTJSON(3);

    @Getter
    private final int type;

    SerializationType(int type) {
        this.type = type;
    }

    public static SerializationType findByType(byte serializationType) {
        for (SerializationType typeEnum : SerializationType.values()) {
            if (typeEnum.getType() == serializationType) {
                return typeEnum;
            }
        }
        return HESSIAN;
    }
}
