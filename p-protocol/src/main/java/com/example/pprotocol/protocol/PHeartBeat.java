package com.example.pprotocol.protocol;

import lombok.Data;
import java.io.Serializable;
@Data
public class PHeartBeat implements Serializable{
    private String message;
}
