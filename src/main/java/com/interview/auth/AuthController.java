package com.interview.auth;

import com.interview.security.JwtUtils;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtils jwtUtils;

    private final Map<String, String> userStore = new ConcurrentHashMap<>();

    public AuthController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
        userStore.put("admin", "admin123");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest req) {
        if (!StringUtils.hasText(req.getUsername()) || !StringUtils.hasText(req.getPassword())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "username/password required"));
        }
        if (userStore.containsKey(req.getUsername())) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "user exists"));
        }
        userStore.put(req.getUsername(), req.getPassword());
        return ResponseEntity.ok(Collections.singletonMap("ok", true));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String pwd = userStore.get(req.getUsername());
        if (pwd == null || !pwd.equals(req.getPassword())) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "invalid credentials"));
        }
        String token = jwtUtils.generateToken(req.getUsername(), Collections.emptyMap());
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}


