package com.example.premoting.handler;

import com.example.pcommon.PFuture;
import com.example.pprotocol.protocol.PResponse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PRequestHolder {
    public final static AtomicLong REQUEST_ID_GENERATOR = new AtomicLong(1);
    public final static ConcurrentHashMap<Long, PFuture<PResponse>> REQUEST_MAP = new ConcurrentHashMap<>();
}
