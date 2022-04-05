package com.example.premoting.handler;

import com.example.pprotocol.protocol.*;
import com.example.premoting.proxy.ProviderInvoker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionException;

@Slf4j
public class PRequestHandler extends SimpleChannelInboundHandler<PProtocol<Object>> {
    private final ProviderInvoker providerInvoker;

    public PRequestHandler(ProviderInvoker providerInvoker) {
        this.providerInvoker = providerInvoker;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PProtocol<Object> msg) {
        if (msg.getBody() instanceof PRequest) {
            try{
                PRequestProcessor.submitRequest(() -> {
                    PProtocol<PResponse> pProtocol = new PProtocol<>();
                    PResponse pResponse = new PResponse();

                    // 修改状态、报文类型和长度
                    MsgHeader header = msg.getMsgHeader();
                    header.setMsgType((byte) MsgType.RESPONSE.getType());


                    try {
                        Object res = providerInvoker.handle((PRequest) msg.getBody());
                        pResponse.setData(res);
                        header.setStatus((byte) MsgStatus.SUCCESS.getCode());

                    } catch (Throwable e) {
                        header.setStatus((byte) MsgStatus.FAIL.getCode());
                        pResponse.setMessage(e.toString());
                        log.error("process request {} error", header.getRequestId(), e);
                    }

                    pProtocol.setMsgHeader(header);
                    pProtocol.setBody(pResponse);
                    ctx.writeAndFlush(pProtocol);
                });

            } catch (RejectedExecutionException e){
                // 抛弃请求后，让调用者超时，进行fail_fast或者fail_over
                MsgHeader header = msg.getMsgHeader();
                log.error("process request {} abort", header.getRequestId(), e);
            }



        }

    }

}
