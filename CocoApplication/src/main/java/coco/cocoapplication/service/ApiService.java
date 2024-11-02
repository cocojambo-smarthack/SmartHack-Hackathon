package coco.cocoapplication.service;

import coco.cocoapplication.model.Session;

public interface ApiService {
    public Session startSession();
    public String endSession();
    public void playRound(String sessionId, String body);
}
