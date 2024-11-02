package coco.cocoapplication.service;

import coco.cocoapplication.helper.Constants;
import coco.cocoapplication.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    private final RestTemplate restTemplate;

    @Value("${external.api.base-url}")
    private String baseUrl;

    @Value("${external.api.key}")
    private String apiKey;

    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Session startSession() {
        String url = baseUrl + Constants.SESSION_START;
        String postUrl = String.format("%s?%s=%s", url, "API_KEY", apiKey);
        Session session = new Session();
        try {
            return restTemplate.postForObject(postUrl, session, Session.class);
        } catch (RestClientException e) {
            logger.error("Error occurred while starting session", e);
            return null;
        }
    }
}
