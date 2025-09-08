package com.interview.chat;

import org.springframework.stereotype.Service;

@Service
public class ChatLogService {
    private final ChatLogMapper mapper;
    public ChatLogService(ChatLogMapper mapper) { this.mapper = mapper; }

    public void save(String username, String channel, String sessionId, String userMsg, String aiMsg){
        ChatLog log = new ChatLog();
        log.setUsername(username);
        log.setChannel(channel);
        log.setSessionId(sessionId);
        log.setUserMessage(userMsg);
        log.setAiMessage(aiMsg);
        mapper.insert(log);
    }
}


