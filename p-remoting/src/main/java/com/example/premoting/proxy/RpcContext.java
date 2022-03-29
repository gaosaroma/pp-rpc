package com.example.premoting.proxy;

import com.example.pcommon.PFuture;
import com.example.pprotocol.protocol.PResponse;

public class RpcContext {
    private static final ThreadLocal<PFuture<PResponse>> LOCAL = new ThreadLocal<PFuture<PResponse>>(

    ) {
        @Override
        protected PFuture<PResponse> initialValue() {
            return null;
        }
    };


    public static PFuture<PResponse> getContext() {
        return LOCAL.get();
    }

    public static void setContext(PFuture<PResponse> future) {
        LOCAL.set(future);
    }

    public static void removeContext() {
        LOCAL.remove();
    }
}
