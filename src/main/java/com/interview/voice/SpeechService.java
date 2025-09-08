package com.interview.voice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class SpeechService {

    @Value("${speech.voskModelPath:C:/models/vosk-model-small-cn-0.22}")
    private String voskModelPath;

    private Model model;

    @PostConstruct
    public void init() throws IOException {
        model = new Model(voskModelPath);
    }

    public String sttPcm16kMono(byte[] wavOrPcm) throws IOException {
        try (Recognizer recognizer = new Recognizer(model, 16000)) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(wavOrPcm)) {
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) >= 0) {
                    if (recognizer.acceptWaveForm(buf, n)) {
                        // partial flush
                    }
                }
                String result = recognizer.getFinalResult();
                // result is JSON: {"text":"..."}
                int idx = result.indexOf("\"text\":\"");
                if (idx > 0) {
                    int start = idx + 8;
                    int end = result.indexOf("\"", start);
                    if (end > start) {
                        return result.substring(start, end);
                    }
                }
                return result;
            }
        }
    }
}


