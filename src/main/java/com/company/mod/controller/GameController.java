package com.company.mod.controller;

import com.company.mod.model.Game;
import com.company.mod.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {
    @Autowired
    private GameService gameService;

    @PostMapping
    public ResponseEntity<Game> createGame() {
        Game game = gameService.createGame();
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<Game> joinGame(@PathVariable String gameId, Authentication authentication) {
        Game game = gameService.joinGame(gameId, authentication.getName());
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/start")
    public ResponseEntity<Game> startGame(@PathVariable String gameId, Authentication authentication) {
        Game game = gameService.startGame(gameId);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<Game> makeMove(@PathVariable String gameId,
                                         @RequestBody Map<String, Object> moveData,
                                         Authentication authentication) {
        Game game = gameService.makeMove(gameId, authentication.getName(), moveData);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/end-phase")
    public ResponseEntity<Game> endPhase(@PathVariable String gameId, Authentication authentication) {
        Game game = gameService.endPhase(gameId, authentication.getName());
        return ResponseEntity.ok(game);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGameState(@PathVariable String gameId) {
        Game game = gameService.getGameState(gameId);
        return ResponseEntity.ok(game);
    }
}
