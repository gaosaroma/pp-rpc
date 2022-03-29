package com.example.premoting.handler;

import com.example.pcommon.PFuture;
import com.example.pprotocol.protocol.PProtocol;
import com.example.pprotocol.protocol.PResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class ShortPResponseHandler extends SimpleChannelInboundHandler<PProtocol<PResponse>> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PProtocol<PResponse> response) throws Exception {
        long requestId = response.getMsgHeader().getRequestId();
        PFuture<PResponse> future = PRequestHolder.REQUEST_MAP.remove(requestId);
        future.getPromise().setSuccess(response.getBody());
        ctx.close();
    }
}
