package coco.cocoapplication.service;

import coco.cocoapplication.model.RoundResponse;
import coco.cocoapplication.model.Session;

public interface SessionService {
    public Session startSession();
    public String endSession();
    public RoundResponse playRound(String sessionId, String body);
    public Session getSession();
}
