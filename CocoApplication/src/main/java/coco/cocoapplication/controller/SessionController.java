package coco.cocoapplication.controller;

import coco.cocoapplication.service.SessionService;
import coco.cocoapplication.service.impl.GreedyServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class SessionController {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private GreedyServiceImpl greedyServiceImpl;

    @PostMapping("/start-session")
    @ResponseBody
    public String startSession() throws JsonProcessingException {
        return sessionService.startSession().id;
    }

    @PostMapping("/play-game")
    @ResponseBody
    public void playGame() throws JsonProcessingException {
        greedyServiceImpl.startRandom();
    }

    @GetMapping("/get-round-moves")
    @ResponseBody
    public String getRoundMoves(@RequestParam("round") int round) {
        return greedyServiceImpl.getRoundMoves(round);
    }

    @PostMapping("/end-session")
    @ResponseBody
    public void endSession() {
        sessionService.endSession();
    }
}
