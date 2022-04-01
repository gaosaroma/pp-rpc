package com.example.premoting.remote;

import com.example.pcommon.ServiceInfo;
import com.example.pprotocol.protocol.PProtocol;
import com.example.pprotocol.protocol.PRequest;
import com.example.premoting.coder.PDecoder;
import com.example.premoting.coder.PEncoder;
import com.example.premoting.handler.ShortPResponseHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShortConnectionClient extends Client {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    // TODO ConcurrentHashMap<String,ChannelFuture> -> ConcurrentHashMap<String,List<ChannelFuture>> for failover and forking
    private static volatile ShortConnectionClient instance = null;

    public static ShortConnectionClient getInstance() {
        if (instance == null) {
            synchronized (LongConnectionClient.class) {
                if (instance == null) {
                    instance = new ShortConnectionClient();
                }
            }
        }
        return instance;
    }

    public ShortConnectionClient() {

        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()*2);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new PEncoder())
                                .addLast(new PDecoder())
                                .addLast(new ShortPResponseHandler());
                    }
                });
    }

    @Override
    public void sendRequest(PProtocol<PRequest> protocol, ServiceInfo serviceInfo) throws Exception {//        ServiceMeta serviceMeta = lookup(protocol, registryService);
        if (serviceInfo != null) {
            ChannelFuture future = bootstrap.connect(serviceInfo.getAddr(), serviceInfo.getPort()).sync();
            future.addListener((ChannelFutureListener) arg0 -> {
                if (future.isSuccess()) {
                    log.info("connect rpc server {} : {} success.", serviceInfo.getAddr(), serviceInfo.getPort());
                } else {
                    log.error("connect rpc server {} : {} failed.", serviceInfo.getAddr(), serviceInfo.getPort());
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });

            future.channel().writeAndFlush(protocol);

        }
    }
}
