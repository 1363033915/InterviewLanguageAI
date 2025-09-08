package com.interview.agent;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentSessionManager {
    private final Map<String, AgentSession> sessions = new ConcurrentHashMap<>();

    public AgentSession create(String username, String role, String level){
        AgentSession s = new AgentSession();
        s.setUsername(username);
        if(role!=null) s.setRole(role);
        if(level!=null) s.setLevel(level);
        sessions.put(s.getSessionId(), s);
        return s;
    }

    public AgentSession get(String id){ return sessions.get(id); }
}


