package coco.cocoapplication.service.impl;

import coco.cocoapplication.model.*;
import coco.cocoapplication.service.ApiService;
import coco.cocoapplication.service.GreedyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GreedyServiceImpl implements GreedyService {
    final static Logger logger = LoggerFactory.getLogger(GreedyServiceImpl.class);

    private final ApiService apiService;

    public List<Refinery> refineries;
    public List<Tank> tanks;
    public List<Customer> customers;
    public List<Connection> connections;

    @Autowired
    public GreedyServiceImpl(ApiServiceImpl apiService) {
        this.apiService = apiService;
    }

    @Override
    public void startRandom() {
        Session session = apiService.startSession();
        logger.info("Started session with id {}", session.id);

        // Send a round with no movements
        Round round = new Round();
        round.day = 0;
        round.movements = new ArrayList<>();

        // Convert the round object to a JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(round);
            logger.info("Sending round: {}", jsonString);
            apiService.playRound(session.id, jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // End the session and log the result
        String answer = apiService.endSession();
        logger.info("Session ended with result: {}", answer);
    }
}
