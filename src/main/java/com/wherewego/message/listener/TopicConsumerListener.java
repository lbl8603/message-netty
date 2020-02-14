package com.wherewego.message.listener;

import com.wherewego.message.conn.NettyUserConnect;
import com.wherewego.message.entity.MsgVO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.sf.json.JSONObject;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * 消息监听
 * @Author:lubeilin
 * @Date:Created in 19:18 2020/2/13
 * @Modified By:
 */
@Component
public class TopicConsumerListener {
    //topic模式的消费者
    @JmsListener(destination="${spring.activemq.topic-name}", containerFactory="topicListener")
    public void readActiveQueue(String message) {
        JSONObject jsonObject = JSONObject.fromObject(message);
        MsgVO msgVO = (MsgVO)JSONObject.toBean(jsonObject,MsgVO.class);
        Channel channel = NettyUserConnect.get(msgVO.getCode());
        if(channel!=null&&channel.isActive()){
            channel.writeAndFlush(new TextWebSocketFrame(message));
        }
    }
}
