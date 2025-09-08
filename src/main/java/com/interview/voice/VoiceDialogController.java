package com.interview.voice;

import com.interview.ai.AIService;
import com.microsoft.cognitiveservices.speech.easytts.EdgeTTS;
import com.microsoft.cognitiveservices.speech.easytts.Voice;
import com.interview.chat.ChatLogService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/voice")
public class VoiceDialogController {

    private final SpeechService speechService;
    private final AIService aiService;
    private final ChatLogService chatLogService;

    public VoiceDialogController(SpeechService speechService, AIService aiService, ChatLogService chatLogService) {
        this.speechService = speechService;
        this.aiService = aiService;
        this.chatLogService = chatLogService;
    }

    @PostMapping(value = "/dialog", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> dialog(@AuthenticationPrincipal User user,
                                                      @RequestBody byte[] audioBytes) throws Exception {
        String text = speechService.sttPcm16kMono(audioBytes);
        String answer = aiService.chat(text);
        byte[] tts = new EdgeTTS().setVoice(Voice.zhCN_XiaoxiaoNeural).speakBytes(answer, StandardCharsets.UTF_8);
        Map<String, Object> res = new HashMap<>();
        res.put("transcript", text);
        res.put("answer", answer);
        res.put("audioWavBase64", Base64Utils.encodeToString(tts));
        chatLogService.save(user.getUsername(), "voice", null, text, answer);
        return ResponseEntity.ok(res);
    }
}


