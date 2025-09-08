package com.interview.webstocket.handeler;

import com.interview.ai.AIService;
import com.interview.voice.SpeechService;
import com.microsoft.cognitiveservices.speech.easytts.EdgeTTS;
import com.microsoft.cognitiveservices.speech.easytts.Voice;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@ChannelHandler.Sharable
public class BinaryWebSocketFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(BinaryWebSocketFrameHandler.class);

    @Autowired
    private SpeechService speechService;

    @Autowired
    private AIService aiService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {
        Channel channel = ctx.channel();
        ByteBuf content = frame.content();
        byte[] audio = new byte[content.readableBytes()];
        content.readBytes(audio);
        String username = channel.attr(AttributeKey.valueOf("username")).get();

        try {
            String transcript = speechService.sttPcm16kMono(audio);
            String answer = aiService.chat(transcript);

            // Send a text frame with transcript and answer (simple JSON)
            String json = "{\"transcript\":\"" + escape(transcript) + "\",\"answer\":\"" + escape(answer) + "\"}";
            channel.writeAndFlush(new TextWebSocketFrame(json));

            // Synthesize TTS and send as binary frame (WAV)
            byte[] tts = new EdgeTTS().setVoice(Voice.zhCN_XiaoxiaoNeural).speakBytes(answer, StandardCharsets.UTF_8);
            channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(tts)));
        } catch (Exception e) {
            logger.error("WS binary handling error", e);
            channel.writeAndFlush(new TextWebSocketFrame("{\"error\":\"" + escape(e.getMessage()) + "\"}"));
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}


