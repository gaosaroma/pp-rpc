package com.example.premoting.handler;

import com.example.pprotocol.protocol.*;
import com.example.pcommon.SerializationType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

//        log.info("send heartbeat");
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state()== IdleState.WRITER_IDLE){
                PProtocol<PHeartBeat> protocol = new PProtocol<>();

                MsgHeader header = new MsgHeader();
                header.setVersion(ProtocolConst.VERSION);
                header.setRequestId(0);
                header.setSerialization((byte) SerializationType.HESSIAN.getType());
                header.setMsgType((byte) MsgType.HEARTBEAT.getType());
                header.setStatus((byte) 0x1);
                protocol.setMsgHeader(header);

                PHeartBeat heartBeat= new PHeartBeat();
                heartBeat.setMessage("ping");
                protocol.setBody(heartBeat);
                ctx.writeAndFlush(protocol);
            }
        }
    }
}
