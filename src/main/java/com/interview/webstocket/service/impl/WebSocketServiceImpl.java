package com.interview.webstocket.service.impl;

import com.interview.webstocket.service.WebSocketService;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <br/>
 * Created by zhu on 2025/4/27
 */
public class WebSocketServiceImpl implements WebSocketService {

    /**
     * 所有已连接的websocket连接列表和一些额外参数
     */
    private static final ConcurrentHashMap<Channel, String> ONLINE_WS_MAP = new ConcurrentHashMap<>();

    /**
     * 所有在线的用户和对应的socket
     */
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<Channel>> ONLINE_UID_MAP = new ConcurrentHashMap<>();


    @Override
    public void connect(Channel channel) {
        ONLINE_WS_MAP.put(channel, null);
    }

    @Override
    public void removed(Channel channel) {
        ONLINE_WS_MAP.remove(channel);
    }


}
