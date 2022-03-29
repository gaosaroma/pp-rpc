package com.example.pprotocol.protocol;

import lombok.Getter;

public enum MsgStatus {
    SUCCESS(0),FAIL(1);
    @Getter
    private int code;

    MsgStatus(int code){
        this.code=code;
    }
}
