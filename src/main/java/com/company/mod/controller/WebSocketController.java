package com.company.mod.controller;

import com.company.mod.model.Game;
import com.company.mod.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameService gameService;

    /**
     * Метод отправляет текущее состояние игры всем подписчикам на тему
     * /topic/game/{gameId}
     */
    public void broadcastGameState(String gameId) {
        Game game = gameService.getGameState(gameId);
        messagingTemplate.convertAndSend("/topic/game/" + gameId, game);
    }
}
