package com.interview.webstocket.handeler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interview.security.JwtUtils;
import io.jsonwebtoken.Claims;

import java.util.Optional;

/**
 * WebSocket 握手处理器
 * 负责提取 HTTP 请求中的设备信息，并设置到 Channel 属性中
 */
@Component
public class WebSocketHandshakeHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandshakeHandler.class);

    public static final AttributeKey<String> SESSION_ID = AttributeKey.valueOf("sessionId");
    public static final AttributeKey<String> DEVICE_ID = AttributeKey.valueOf("deviceId");

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("握手");
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;

            // 提取设备ID
            String deviceId = req.headers().get("device-id");

            // 如果请求头中没有 device-id，尝试从 URL 查询参数中获取
            if (deviceId == null || deviceId.isEmpty()) {
                // 解析 URL 查询参数
                QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
                deviceId = Optional.ofNullable(decoder.parameters().get("device_id"))
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.get(0))
                        .orElse(null);
            }

            if (deviceId == null || deviceId.isEmpty()) {
                logger.warn("WebSocket连接缺少device-id头");
                ctx.close();
                return;
            }

            // 校验 JWT: 支持 Authorization: Bearer xxx 或 query: token
            String authHeader = req.headers().get("Authorization");
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
                token = Optional.ofNullable(decoder.parameters().get("token"))
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.get(0))
                        .orElse(null);
            }
            if (token == null || token.isEmpty()) {
                logger.warn("WebSocket连接缺少JWT token");
                ctx.close();
                return;
            }
            try {
                Claims claims = jwtUtils.parseToken(token);
                ctx.channel().attr(AttributeKey.valueOf("username")).set(claims.getSubject());
            } catch (Exception e) {
                logger.warn("JWT无效: {}", e.getMessage());
                ctx.close();
                return;
            }

            // 生成会话ID并存储
            String sessionId = ctx.channel().id().asShortText();
            ctx.channel().attr(SESSION_ID).set(sessionId);
            ctx.channel().attr(DEVICE_ID).set(deviceId);

            logger.info("WebSocket握手请求 - SessionId: {}, DeviceId: {}", sessionId, deviceId);
        }

        // 继续处理请求
        ctx.fireChannelRead(msg);
    }
}
