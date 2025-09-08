package com.interview.webstocket.handeler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.interview.ai.AIService;
import com.interview.chat.ChatLogService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <br/>
 * Created by zhu on 2025/4/27
 */
@Component
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(TextWebSocketFrameHandler.class);
    @Autowired
    private AIService aiService;
    @Autowired
    private ChatLogService chatLogService;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        logger.info("消息={}", textWebSocketFrame.text());
        Channel channel = channelHandlerContext.channel();
        Attribute<String> usernameAttr = channel.attr(AttributeKey.valueOf("username"));
        String username = usernameAttr.get();
        String userText = textWebSocketFrame.text();
        String reply = aiService != null ? aiService.chat(userText) : "AI not available";
        channel.writeAndFlush(new TextWebSocketFrame(reply));
        try { if(chatLogService!=null) chatLogService.save(username, "ws", null, userText, reply); } catch (Exception ignored) {}
    }
}
