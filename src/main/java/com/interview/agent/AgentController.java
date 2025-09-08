package com.interview.agent;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> start(@AuthenticationPrincipal User user,
                                                     @RequestBody StartReq req){
        AgentSession s = agentService.startSession(user.getUsername(), req.getRole(), req.getLevel());
        return ResponseEntity.ok(Collections.singletonMap("sessionId", s.getSessionId()));
    }

    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> message(@AuthenticationPrincipal User user,
                                                       @RequestBody MsgReq req){
        String reply = agentService.message(req.getSessionId(), req.getMessage());
        return ResponseEntity.ok(Collections.singletonMap("answer", reply));
    }

    @Data
    public static class StartReq { private String role; private String level; }
    @Data
    public static class MsgReq { private String sessionId; private String message; }
}


