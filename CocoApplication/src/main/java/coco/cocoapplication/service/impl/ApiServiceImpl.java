package coco.cocoapplication.service.impl;

import coco.cocoapplication.helper.Constants;
import coco.cocoapplication.helper.JsonToRoundResponseConverter;
import coco.cocoapplication.model.RoundResponse;
import coco.cocoapplication.model.Session;
import coco.cocoapplication.service.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiServiceImpl implements ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ApiServiceImpl.class);
    private final RestTemplate restTemplate;

    @Value("${external.api.base-url}")
    private String baseUrl;

    @Value("${external.api.key}")
    private String apiKey;

    public ApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String sendRequest(String url, HttpEntity<String> entity) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            logger.info("Response body: {}", response.getBody());
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Failed to send request", e);
            return null;
        }
    }

    @Override
    public Session startSession() {
        String url = baseUrl + Constants.SESSION_START;

        HttpHeaders headers = new HttpHeaders();
        headers.set("API-KEY", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String response = sendRequest(url, entity);
        if (response == null)
            return null;

        return new Session(response);
    }

    @Override
    public String endSession() {
        String url = baseUrl + Constants.SESSION_END;

        HttpHeaders headers = new HttpHeaders();
        headers.set("API-KEY", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return sendRequest(url, entity);
    }

    @Override
    public RoundResponse playRound(String sessionId, String body) {
        String url = baseUrl + Constants.PLAY_ROUND;

        HttpHeaders headers = new HttpHeaders();
        headers.set("API-KEY", apiKey);
        headers.set("SESSION-ID", sessionId);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return JsonToRoundResponseConverter.convertJsonToRoundResponse(sendRequest(url, entity));
    }
}
