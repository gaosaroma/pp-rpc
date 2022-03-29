package com.example.premoting.coder;

import com.example.pprotocol.protocol.MsgHeader;
import com.example.pprotocol.protocol.PProtocol;
import com.example.pprotocol.serialization.PSerialization;
import com.example.pprotocol.serialization.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PEncoder extends MessageToByteEncoder<PProtocol<Object>> {

    /*
    +---------------------------------------------------------------+
    | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |  状态 1byte |
    +---------------------------------------------------------------+
    |         请求 ID 8byte     |      数据长度 4byte     |
    +---------------------------------------------------------------+
    */

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, PProtocol<Object> msg, ByteBuf out) throws Exception {
//        log.info("encode msg");
        MsgHeader header = msg.getMsgHeader();
        out.writeByte(header.getVersion());
        out.writeByte(header.getSerialization());
        out.writeByte(header.getMsgType());
        out.writeByte(header.getStatus());
        out.writeLong(header.getRequestId());

        PSerialization pSerialization = SerializationFactory.getPSerialization(header.getSerialization());
        byte[] data = pSerialization.serialize(msg.getBody());
//        log.info("serialization success");
        out.writeInt(data.length);
//        log.info("data length {}",data.length);
        out.writeBytes(data);
    }
}
