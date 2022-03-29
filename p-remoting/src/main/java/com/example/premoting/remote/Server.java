package com.example.premoting.remote;

import com.example.pcommon.RpcServiceUtil;
import com.example.pcommon.ServiceInfo;
import com.example.premoting.coder.PDecoder;
import com.example.premoting.coder.PEncoder;
import com.example.premoting.handler.HeartBeatServerHandler;
import com.example.premoting.handler.PRequestHandler;
import com.example.pcommon.annotation.PService;
import com.example.pregistry.RegistryService;
import com.example.premoting.proxy.ProviderInvoker;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
// bean实例化后
public class Server implements InitializingBean, BeanPostProcessor {
    private String serverAddress;
    private final int serverPort;
    private final RegistryService registryService;
    private final Map<String, Object> rpcServiceMap = new HashMap<>();
    private final ProviderInvoker providerInvoker =new ProviderInvoker(rpcServiceMap);


    public Server(int serverPort, RegistryService registryService) {
        this.serverPort = serverPort;
        this.registryService = registryService;
    }


    private void startServer() throws Exception {

        this.serverAddress = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();


        try {

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)

                    .channel(NioServerSocketChannel.class)

                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override

                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            socketChannel.pipeline()
                                    .addLast(new IdleStateHandler(180, 0, 200, TimeUnit.SECONDS))
                                    .addLast(new HeartBeatServerHandler())
                                    .addLast(new PEncoder())
                                    .addLast(new PDecoder())
                                    .addLast(new PRequestHandler(providerInvoker));
                        }

                    });

            ChannelFuture channelFuture = bootstrap.bind(this.serverAddress, this.serverPort).sync();

            log.info("server: Addr {}, Port {}", this.serverAddress, this.serverPort);

            channelFuture.channel().closeFuture().sync();

        } finally {


            boss.shutdownGracefully();

            worker.shutdownGracefully();

        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            try {
                startServer();
            } catch (Exception e) {
                log.error("start server failed", e);
            }
        }).start();
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {//        log.info("postProcessAfterInitialization and register service");
        PService pService = bean.getClass().getAnnotation(PService.class);
        if (pService != null) {
            String serviceName = pService.serviceInterface().getName();
            String serviceVersion = pService.serviceVersion();

            try {
                ServiceInfo serviceInfo = new ServiceInfo();
                serviceInfo.setAddr(this.serverAddress);
                serviceInfo.setPort(this.serverPort);
                serviceInfo.setVersion(serviceVersion);
                serviceInfo.setName(serviceName);

                this.registryService.register(serviceInfo);
                rpcServiceMap.put(RpcServiceUtil.buildServiceKey(serviceName, serviceVersion), bean);

            } catch (Exception e) {
                log.error("register failed: service {}#{}", serviceName, serviceVersion, e);
            }
        }
        return bean;
    }
}
