package com.interview.agent;

import com.interview.ai.AIService;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final AgentSessionManager sessionManager;
    private final PromptBuilder promptBuilder;
    private final AIService aiService;

    public AgentService(AgentSessionManager sessionManager, PromptBuilder promptBuilder, AIService aiService) {
        this.sessionManager = sessionManager;
        this.promptBuilder = promptBuilder;
        this.aiService = aiService;
    }

    public AgentModels.AgentSession startSession(String username, String role, String level){
        return sessionManager.create(username, role, level);
    }

    public String message(String sessionId, String userMessage){
        AgentModels.AgentSession s = sessionManager.get(sessionId);
        if(s == null) return "会话不存在";
        String system = promptBuilder.buildSystemPrompt(s.getRole(), s.getLevel());
        String reply = aiService.chatWithSystem(system, userMessage);
        AgentModels.Turn turn = new AgentModels.Turn(); turn.setUser(userMessage); turn.setAssistant(reply); s.getHistory().add(turn);
        return reply;
    }
}


