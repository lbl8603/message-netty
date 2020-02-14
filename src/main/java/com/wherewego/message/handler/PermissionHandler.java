package com.wherewego.message.handler;

import com.wherewego.message.conn.NettyUserConnect;
import com.wherewego.message.utils.MD5Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


/**
 * websocket权限校验，遇到http连接则分发到下一级
 * @Author:lubeilin
 * @Date:Created in 16:09 2020/2/13
 * @Modified By:
 */
@Component
@ChannelHandler.Sharable
public class PermissionHandler extends ChannelHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionHandler.class);
    private WebSocketServerHandshaker handshaker;
    @Autowired
    private ClientHandler clientHandler;

    //客户端连接地址
    @Value("${msg.websocket.netty.clientPath}")
    private String receive;

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
        NettyUserConnect.remove(ctx.channel().id());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("连接通信"+msg.getClass().getName());
        if (msg instanceof FullHttpRequest) {

            FullHttpRequest request = (FullHttpRequest) msg;
            if (!request.getDecoderResult().isSuccess() || (!"websocket".equals(request.headers().get("Upgrade")))) {
//                ctx.pipeline().addLast("httpRoute",httpRouteHandler); 转由http处理
                LOGGER.info("http请求");
                ctx.fireChannelRead(request.retain());
                return;
            }
            try {
                String uri = request.getUri();
                if (uri.startsWith(receive+'?')) {
                    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
                    Map<String, List<String>> parameters = queryStringDecoder.parameters();
                    String code = parameters.get("code").get(0);
                    String expire = parameters.get("expire").get(0);
                    String sign = parameters.get("sign").get(0);
                    if(!MD5Util.getMD5(code+"_"+expire).equals(sign)){
                        throw new RuntimeException("校验失败");
                    }
                    NettyUserConnect.add(code,ctx.channel());
//                    request.setUri(receive);

                }else{
                    sendWebSocketResponse(ctx,"url err", request,
                            new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
                    return;
                }

                //完成http握手升级
                WebSocketServerHandshakerFactory wf = new WebSocketServerHandshakerFactory(request.getUri(),null,false);
                handshaker = wf.newHandshaker(request);
                if(handshaker==null){
                    WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
                }else{
                    handshaker.handshake(ctx.channel(),request);
                }
//                ctx.channel().writeAndFlush(new TextWebSocketFrame("连接成功"));
            }catch (Exception e){
                sendWebSocketResponse(ctx,"parameters err", request,
                        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
                return;
            }

        }else if(msg instanceof CloseWebSocketFrame){
            ctx.channel().close();
            NettyUserConnect.remove(ctx.channel().id());
        }
        ctx.fireChannelRead(msg);

    }
    private void sendHttpResponse(ChannelHandlerContext ctx, String msg,FullHttpRequest req, DefaultFullHttpResponse res){
        ByteBuf buf = Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8);
        LOGGER.info(req.retain().headers().get("Origin"));
        res.content().writeBytes(buf);
        res.headers().set("Content-Type","application/json");
        res.headers().set("Access-Control-Allow-Origin", req.retain().headers().get("Origin"));
        res.headers().set("Access-Control-Allow-Credentials", "true");
        buf.release();
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            ctx.channel().writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
        }else{
            res.headers().set("Connection", "keep-alive");
            ctx.channel().writeAndFlush(res);
        }
    }
    private void sendWebSocketResponse(ChannelHandlerContext ctx, String err, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        res.headers().set("error-msg",err);
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        ctx.channel().writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
    }

}
