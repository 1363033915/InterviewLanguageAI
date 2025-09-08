package com.interview.chat;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ChatLog {
    private Long id;
    private String username;
    private String channel; // rest|ws|voice|agent
    private String sessionId;
    private String userMessage;
    private String aiMessage;
    private Instant createdAt = Instant.now();
}


