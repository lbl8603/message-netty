package com.wherewego.message.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteOrder;
import java.util.List;

/**
 * 使用该类处理请求，后续可以扩展支持socket连接
 * @Author:lubeilin
 * @Date:Created in 16:26 2020/2/13
 * @Modified By:
 */
@Component
public class SocketChooseHandler extends ByteToMessageDecoder {
    private final static Logger LOGGER = LoggerFactory.getLogger(SocketChooseHandler.class);
    private ChannelHandlerAdapter clientHandler;
    /** 默认暗号长度为23 */
    private static final int MAX_LENGTH = 23;
    /** WebSocket握手的协议前缀 */
    private static final String HTTP_GET = "GET /";
    private static final String HTTP_POST = "POST /";
    private ChannelHandlerAdapter permissionHandler;
    public SocketChooseHandler(ChannelHandlerAdapter permissionHandler,ChannelHandlerAdapter clientHandler){
        this.permissionHandler = permissionHandler;
        this.clientHandler = clientHandler;
    }
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        ChannelPipeline pipeline = ctx.channel().pipeline();
        String protocol = getBufStart(byteBuf);
        byteBuf.resetReaderIndex();
        LOGGER.debug(protocol);
        if (protocol.startsWith(HTTP_GET)||protocol.startsWith(HTTP_POST)) {
            websocketAdd(pipeline);
        }

        pipeline.remove(this);
    }
    private String getBufStart(ByteBuf in){
        int length = in.readableBytes();
        if (length > MAX_LENGTH) {
            length = MAX_LENGTH;
        }

        // 标记读位置
        in.markReaderIndex();
        byte[] content = new byte[length];
        in.readBytes(content);
        return new String(content);
    }
    public void socketAdd(ChannelPipeline pipeline){
        //Socket 连接心跳检测
        pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 0, 0));
        //注意，这个专门针对 Socket 信息的解码器只能放在 SocketChooseHandler 之后，否则会导致 webSocket 连接出错
        pipeline.addLast("decoder",new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN,1024*1024,0,4,0,4,true));
    }

    public  void websocketAdd(ChannelPipeline pipeline){

        // HttpServerCodec：将请求和应答消息解码为HTTP消息
        pipeline.addLast("http-codec",new HttpServerCodec());

        // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
        pipeline.addLast("aggregator",new HttpObjectAggregator(65535));

        // ChunkedWriteHandler：向客户端发送HTML5文件,文件过大会将内存撑爆
        pipeline.addLast("http-chunked",new ChunkedWriteHandler());

        pipeline.addLast("permissionHandler",permissionHandler);
        pipeline.addLast("client",clientHandler);

        //用于处理websocket, /ws为访问websocket时的uri
        //  pipeline.addLast("protocolHandler", new WebSocketServerProtocolHandler("/ws"));
        // pipeline.addLast("webSocketHandler",webSocketHandler);
    }
}
