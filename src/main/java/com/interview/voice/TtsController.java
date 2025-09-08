package com.interview.voice;

import com.microsoft.cognitiveservices.speech.easytts.EdgeTTS;
import com.microsoft.cognitiveservices.speech.easytts.Voice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/voice")
public class TtsController {

    @PostMapping(value = "/tts", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> tts(@AuthenticationPrincipal User user, @RequestBody String text) throws Exception {
        if (text == null) {
            text = "";
        }
        text = text.trim();
        if (text.isEmpty()) {
            text = "";
        }
        // EdgeTTS synthesizes to bytes (wav). Keep simple default voice.
        byte[] data = new EdgeTTS().setVoice(Voice.zhCN_XiaoxiaoNeural).speakBytes(text, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=tts.wav")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}


