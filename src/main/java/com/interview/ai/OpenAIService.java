package com.interview.ai;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OpenAIService implements AIService {

    private final OkHttpClient client = new OkHttpClient();

    @Value("${ai.apiKey}")
    private String apiKey;

    @Value("${ai.model:gpt-3.5-turbo}")
    private String model;

    @Override
    public String chat(String prompt) {
        try {
            String json = "{\"model\":\"" + model + "\",\"messages\":[{\"role\":\"user\",\"content\":\"" + prompt.replace("\"", "\\\"") + "\"}]}";
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "AI error: HTTP " + response.code();
                }
                String body = response.body() != null ? response.body().string() : "";
                // very naive extraction to avoid pulling full JSON parser here
                int idx = body.indexOf("\"content\":\"");
                if (idx > 0) {
                    int start = idx + 11;
                    int end = body.indexOf("\"", start);
                    if (end > start) {
                        return body.substring(start, end);
                    }
                }
                return body;
            }
        } catch (IOException e) {
            return "AI exception: " + e.getMessage();
        }
    }

    @Override
    public String chatWithSystem(String systemPrompt, String userPrompt) {
        try {
            String json = "{\"model\":\"" + model + "\",\"messages\":[" +
                    "{\"role\":\"system\",\"content\":\"" + systemPrompt.replace("\"", "\\\"") + "\"}," +
                    "{\"role\":\"user\",\"content\":\"" + userPrompt.replace("\"", "\\\"") + "\"}]}";
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "AI error: HTTP " + response.code();
                }
                String body = response.body() != null ? response.body().string() : "";
                int idx = body.indexOf("\"content\":\"");
                if (idx > 0) {
                    int start = idx + 11;
                    int end = body.indexOf("\"", start);
                    if (end > start) {
                        return body.substring(start, end);
                    }
                }
                return body;
            }
        } catch (IOException e) {
            return "AI exception: " + e.getMessage();
        }
    }
}


