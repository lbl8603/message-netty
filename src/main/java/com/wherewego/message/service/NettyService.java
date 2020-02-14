package com.wherewego.message.service;

import com.wherewego.message.handler.ClientHandler;
import com.wherewego.message.handler.PermissionHandler;
import com.wherewego.message.handler.SocketChooseHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
/**
 * 启动netty服务
 * @Author:lubeilin
 * @Date:Created in 16:03 2020/2/13
 * @Modified By:
 */
@Service
public class NettyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyService.class);
    @Value("${msg.server.nettyPort}")
    private int nettyPort;
    @Value("${msg.websocket.netty.path}")
    private String websocketPath;
    private EventLoopGroup bossGroup;
    private EventLoopGroup group;
    @Autowired
    private PermissionHandler permissionHandler;
    @Autowired
    private ClientHandler clientHandler;
    @PostConstruct
    public void initNetty(){
        try {
            run(nettyPort);
        } catch (InterruptedException e) {
            LOGGER.error("启动失败",e);
        }
    }
    public void run(int port) throws InterruptedException {

        LOGGER.info("=========Netty端口启动中========");
        bossGroup = new NioEventLoopGroup();
        group = new NioEventLoopGroup();
        ServerBootstrap sb = new ServerBootstrap();
        sb.option(ChannelOption.SO_BACKLOG, 1024);// 配置TCP参数，握手字符串长度设置
        sb.group(group, bossGroup) // group辅助客户端的tcp连接请求 bossGroup负责与客户端之间的读写操作
                .channel(NioServerSocketChannel.class) // 配置客户端的channel类型
                .localAddress(port)// 绑定监听端口
                .childHandler(new ChannelInitializer<SocketChannel>() { // 绑定客户端连接时候触发操作
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //ch.pipeline().addLast("active",channelActiveHandler);
                        ch.pipeline().addLast("socketChoose",new SocketChooseHandler(permissionHandler,clientHandler));

//                        // websocket协议本身是基于http协议的，所以这边也要使用http解编码器
//                        ch.pipeline().addLast("http-codec",new HttpServerCodec());
//                        ch.pipeline().addLast(new ObjectEncoder());
//                        // 以块的方式来写的处理器
//                        ch.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
//                        ch.pipeline().addLast("aggregator",new HttpObjectAggregator(65536));
//                        ch.pipeline().addLast(permissionHandler);
//                        ch.pipeline().addLast(new WebSocketServerProtocolHandler(websocketPath,WEBSOCKET_PROTOCOL.toString(),true));
//
//                        ch.pipeline().addLast(webSocketServerHandler);
                    }
                });
        // 服务器异步创建绑定
        sb.bind().sync();
        LOGGER.info("=========Netty websocket已启动，端口{}========",port);
    }

    @PreDestroy
    private void destory() throws InterruptedException {
        if (null != bossGroup) {
            bossGroup.shutdownGracefully().sync();
        }
        if (null != group) {
            group.shutdownGracefully().sync();
        }
    }
}
