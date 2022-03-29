package com.example.pconsumer.Controller;


import com.example.pcommon.AsyncType;
import com.example.pcommon.PFuture;
import com.example.pcommon.SerializationType;
import com.example.pcommon.annotation.PReference;
import com.example.pprotocol.protocol.PResponse;
import com.example.premoting.proxy.RpcContext;
import com.example.pservice.PService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@Slf4j
public class RpcController {

    public RpcController() {
        log.info("init RpcController");
    }

    @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
    @PReference(asyncType = AsyncType.ASYNC)
    private PService pService;


    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String tryPP() throws ExecutionException, InterruptedException, TimeoutException {

        pService.helloPP("pp");

        PFuture<PResponse> future = RpcContext.getContext();
        String res = (String) future.getPromise().get(10, TimeUnit.MILLISECONDS).getData();

        RpcContext.removeContext();
        return res;
    }

}
