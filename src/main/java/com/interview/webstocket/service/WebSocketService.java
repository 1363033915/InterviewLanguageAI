package com.interview.webstocket.service;

import io.netty.channel.Channel;

/**
 * <br/>
 * Created by zhu on 2025/4/27
 */
public interface  WebSocketService {

    /**
     * 处理所有ws连接的事件
     *
     * @param channel
     */
    void connect(Channel channel);

    /**
     * 处理ws断开连接的事件
     *
     * @param channel
     */
    void removed(Channel channel);
}
