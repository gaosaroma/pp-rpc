package com.example.premoting.coder;

import com.example.pprotocol.protocol.*;
import com.example.pprotocol.serialization.PSerialization;
import com.example.pprotocol.serialization.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
public class PDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() < ProtocolConst.HEADER_TOTAL_LEN) {
            return;
        }
        in.markReaderIndex();

        byte version = in.readByte();
        byte serializeType = in.readByte();
        byte msgType = in.readByte();
        byte status = in.readByte();
        long requestId = in.readLong();

        int dataLength = in.readInt();

        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        MsgType msgTypeEnum = MsgType.findByType(msgType);

        if (msgTypeEnum == null) {
            return;
        }

        MsgHeader header = new MsgHeader();
        header.setVersion(version);
        header.setSerialization(serializeType);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setMsgLen(dataLength);


        PSerialization pSerialization = SerializationFactory.getPSerialization(serializeType);

        switch (msgTypeEnum) {
            case REQUEST:
                PRequest request = pSerialization.deserialize(data, PRequest.class);
                if (request != null) {
                    PProtocol<PRequest> protocol = new PProtocol<>();
                    protocol.setMsgHeader(header);
                    protocol.setBody(request);
                    out.add(protocol);
                }
                break;
            case RESPONSE:
                PResponse response = pSerialization.deserialize(data, PResponse.class);
                if (response != null) {
                    PProtocol<PResponse> protocol = new PProtocol<>();
                    protocol.setMsgHeader(header);
                    protocol.setBody(response);
                    out.add(protocol);
                }
                break;
            case HEARTBEAT:
                PHeartBeat heartBeat = pSerialization.deserialize(data, PHeartBeat.class);
                if (heartBeat != null) {
                    PProtocol<PHeartBeat> protocol = new PProtocol<>();
                    protocol.setMsgHeader(header);
                    protocol.setBody(heartBeat);
                    out.add(protocol);
                }
                break;
        }
    }
}
