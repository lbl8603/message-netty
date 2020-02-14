package com.wherewego.message.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * 处理websocket事件
 * @Author:lubeilin
 * @Date:Created in 16:44 2020/2/13
 * @Modified By:
 */
@Component
@ChannelHandler.Sharable
public class ClientHandler  extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        ctx.channel().writeAndFlush(new TextWebSocketFrame("服务端已收到信息") );
    }
}
