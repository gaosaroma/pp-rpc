package com.example.premoting.handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PRequestProcessor {
    private static volatile ThreadPoolExecutor threadPoolExecutor;

    public static void submitRequest(Runnable task) throws RejectedExecutionException {
        if(null==threadPoolExecutor){
            synchronized (PRequestProcessor.class){
                if(null==threadPoolExecutor){
                    threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors()+1,Runtime.getRuntime().availableProcessors()+1,60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100000));
                }
            }
        }
        threadPoolExecutor.submit(task);
    }
}
