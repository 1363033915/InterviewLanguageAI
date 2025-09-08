package com.interview.agent;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class AgentSession {
    private String sessionId = UUID.randomUUID().toString();
    private String username;
    private String role = "backend"; // e.g., backend, frontend, fullstack
    private String level = "mid"; // junior/mid/senior
    private List<Turn> history = new ArrayList<>();
    private Instant createdAt = Instant.now();
}

@Data
class Turn {
    private String user;
    private String assistant;
}


