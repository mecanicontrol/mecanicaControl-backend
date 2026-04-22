package cl.mecanicontrol.backend.domain.diagnostico.service.external;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroqService {
    @Value("${groq.api.key}")
    private String groqApiKey;

    private static final String GROQ_URL = 
        "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String llamar(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> body = Map.of(
            "model", MODEL,
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.3,
            "max_tokens", 1024
        );

        HttpEntity<Map<String, Object>> entity = 
            new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = 
                restTemplate.postForEntity(GROQ_URL, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices")
                       .get(0)
                       .path("message")
                       .path("content")
                       .asText();

        } catch (Exception e) {
            log.error("Error llamando a Groq API: {}", e.getMessage());
            throw new RuntimeException("Error real: " + e.getMessage(), e);
        }
    }
}


