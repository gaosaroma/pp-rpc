package com.example.pcommon;

import io.netty.util.concurrent.Promise;
import lombok.Data;

@Data
public class PFuture<T> {
    private Promise<T> promise;
    private long timeout;

    public PFuture(Promise<T> promise,long timeout){
        this.promise = promise;
        this.timeout = timeout;
    }

}
