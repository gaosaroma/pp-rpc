package com.example.premoting.remote;


import com.example.pcommon.ServiceInfo;
import com.example.premoting.coder.PDecoder;
import com.example.premoting.coder.PEncoder;
import com.example.premoting.handler.HeartBeatClientHandler;
import com.example.premoting.handler.PResponseHandler;
import com.example.pprotocol.protocol.PProtocol;
import com.example.pprotocol.protocol.PRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LongConnectionClient extends Client {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private static volatile LongConnectionClient instance = null;
    // TODO ConcurrentHashMap<String,ChannelFuture> -> ConcurrentHashMap<String,List<ChannelFuture>> for failover and forking
    public static final ConcurrentHashMap<String,ChannelFuture> channelFutureMap = new ConcurrentHashMap<>();

    public static LongConnectionClient getInstance() {
        if (instance == null) {
            synchronized (LongConnectionClient.class) {
                if (instance == null) {
                    instance = new LongConnectionClient();
                }
            }
        }
        return instance;
    }

    private LongConnectionClient() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()*2);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new IdleStateHandler(0, 60, 0, TimeUnit.SECONDS))
                                .addLast(new HeartBeatClientHandler())
                                .addLast(new PEncoder())
                                .addLast(new PDecoder())
                                .addLast(new PResponseHandler());
                    }
                });
    }

    private ChannelFuture doConnect(String server_addr, int server_port) throws InterruptedException {
        ChannelFuture future = bootstrap.connect(server_addr, server_port).sync();
        future.addListener((ChannelFutureListener) arg0 -> {
            if (future.isSuccess()) {
                log.info("connect rpc server {} : {} success.", server_addr, server_port);
            } else {
                log.error("connect rpc server {} : {} failed.", server_addr, server_port);
                future.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });

        return future;
    }

    @Override
    public void sendRequest(PProtocol<PRequest> protocol, ServiceInfo serviceInfo) throws Exception {


        if (serviceInfo != null) {
            String serviceAddrPort = serviceInfo.getAddr() + ':' + serviceInfo.getPort();
            ChannelFuture future;
            if(!channelFutureMap.containsKey(serviceAddrPort)||!channelFutureMap.get(serviceAddrPort).channel().isActive()){
                future = doConnect(serviceInfo.getAddr(),serviceInfo.getPort());
                channelFutureMap.put(serviceAddrPort,future);

            }else {
                future = channelFutureMap.get(serviceAddrPort);

            }

            future.channel().writeAndFlush(protocol);

        }
    }


}
