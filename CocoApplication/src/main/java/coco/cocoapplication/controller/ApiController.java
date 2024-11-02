package coco.cocoapplication.controller;

import coco.cocoapplication.model.Session;
import coco.cocoapplication.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ApiController {
    @Autowired
    private ApiService apiService;

    @PostMapping("/start-session")
    @ResponseBody
    public Session startSession() {
        return apiService.startSession();
    }

    @PostMapping("/end-session")
    @ResponseBody
    public void endSession() {
        apiService.endSession();
    }

    @PostMapping("/play-round")
    @ResponseBody
    public void playRound(@RequestHeader("session-id") String sessionId, @RequestBody String body) {
        apiService.playRound(sessionId, body);
    }
}
