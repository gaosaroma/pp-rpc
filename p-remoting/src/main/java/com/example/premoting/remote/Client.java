package com.example.premoting.remote;

import com.example.pcommon.ServiceInfo;
import com.example.pprotocol.protocol.PProtocol;
import com.example.pprotocol.protocol.PRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Client {
    public abstract void sendRequest(PProtocol<PRequest> protocol, ServiceInfo serviceInfo) throws Exception;
}
