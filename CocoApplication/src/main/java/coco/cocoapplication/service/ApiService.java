package coco.cocoapplication.service;

import coco.cocoapplication.model.RoundResponse;
import coco.cocoapplication.model.Session;

public interface ApiService {
    public Session startSession();
    public String endSession();
    public RoundResponse playRound(String sessionId, String body);
}
