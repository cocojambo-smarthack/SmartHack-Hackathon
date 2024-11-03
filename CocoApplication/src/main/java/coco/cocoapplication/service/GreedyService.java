package coco.cocoapplication.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface GreedyService {
    void precalculate();
    void startRandom() throws JsonProcessingException;
    String getRoundMoves(int round);
}