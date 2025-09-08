package com.interview.webstocket.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

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

    void handleMessage(Channel channel, TextWebSocketFrame frame);
    void handleConnection(Channel channel);
    void handleDisconnection(Channel channel);
    
    // 新增AI对话接口
    String getAIResponse(String userInput);
    
    // 新增语音识别接口
    String speechToText(byte[] audioData);
    
    // 新增语音合成接口
    byte[] textToSpeech(String text);
}
