package com.interview.ai;

public interface AIService {
    String chat(String prompt);

    String chatWithSystem(String systemPrompt, String userPrompt);
}


