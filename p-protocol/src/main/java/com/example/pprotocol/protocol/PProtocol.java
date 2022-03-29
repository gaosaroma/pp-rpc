package com.example.pprotocol.protocol;

import lombok.Data;

import java.io.Serializable;

@Data
public class PProtocol<T> implements Serializable {
    private MsgHeader msgHeader;
    private T body;
}
