package com.wherewego.message.conn;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存客户端连接
 * @Author:lubeilin
 * @Date:Created in 18:30 2020/2/13
 * @Modified By:
 */
public class NettyUserConnect {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyUserConnect.class);
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static Map<String,ChannelId> channelIDMap = new ConcurrentHashMap<>();
    private static Map<ChannelId,String> channelIDTOCodeMap = new ConcurrentHashMap<>();
    public static synchronized void add(String code,Channel channel){
        channelGroup.add(channel);
        channelIDTOCodeMap.put(channel.id(),code);
        channelIDMap.put(code,channel.id());
    }
    public static synchronized void remove(String code){
        if(code!=null){
            ChannelId channelId = channelIDMap.remove(code);
            if(channelId!=null){
                channelGroup.remove(channelId);
                channelIDTOCodeMap.remove(channelId);
            }
        }

    }
    public static synchronized void remove(ChannelId channelId){
        if(channelId!=null){
            String  code = channelIDTOCodeMap.remove(channelId);
            if(code!=null)
                channelIDMap.remove(code);
            channelGroup.remove(channelId);
        }

    }
    public static Channel get(String code){
        ChannelId channelId = channelIDMap.get(code);
        if(channelId!=null)
            return channelGroup.find(channelId);
        return null;
    }
    public static ChannelGroup getChannelGroup(){
        return channelGroup;
    }

}

