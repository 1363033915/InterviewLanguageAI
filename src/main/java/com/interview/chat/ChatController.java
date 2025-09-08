package com.interview.chat;

import com.interview.ai.AIService;
import com.interview.chat.ChatLogService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AIService aiService;
    private final ChatLogService chatLogService;

    public ChatController(AIService aiService, ChatLogService chatLogService) {
        this.aiService = aiService;
        this.chatLogService = chatLogService;
    }

    @PostMapping("/text")
    public ResponseEntity<Map<String, String>> chat(@AuthenticationPrincipal User user,
                                                    @RequestBody ChatRequest req) {
        String answer = aiService.chat(req.getMessage());
        chatLogService.save(user.getUsername(), "rest", null, req.getMessage(), answer);
        return ResponseEntity.ok(Collections.singletonMap("answer", answer));
    }

    @Data
    public static class ChatRequest {
        private String message;
    }
}


