package com.company.mod.service;

import com.company.mod.model.Game;
import com.company.mod.model.GamePhase;
import com.company.mod.model.Move;
import com.company.mod.model.Player;
import com.company.mod.repository.GameRepository;
import com.company.mod.controller.WebSocketController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    @Lazy
    private WebSocketController gameWebSocketController;

    @Transactional
    public Game createGame() {
        Game game = new Game();
        game = gameRepository.save(game);
        gameWebSocketController.broadcastGameState(game.getId());
        return game;
    }

    @Transactional
    public Game joinGame(String gameId, String playerName) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        if (game.getCurrentPhase() != GamePhase.WAITING) {
            throw new RuntimeException("Game already started");
        }
        boolean exists = game.getPlayers().stream()
                .anyMatch(p -> p.getName().equals(playerName));
        if (!exists) {
            Player player = new Player(playerName);
            player.setGame(game);
            game.addPlayer(player);
            game.addMove(Move.createJoinGameMove(game, player));
        }
        game = gameRepository.save(game);
        gameWebSocketController.broadcastGameState(game.getId());
        return game;
    }

    @Transactional
    public Game startGame(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        if (game.getPlayers().size() < 2) {
            throw new RuntimeException("Not enough players");
        }
        if (game.getCurrentPhase() != GamePhase.WAITING) {
            return game;
        }
        log.info("Attempting to start game " + gameId);
        game.nextPhase();
        game = gameRepository.save(game);
        gameWebSocketController.broadcastGameState(game.getId());
        return game;
    }

    @Transactional
    public Game makeMove(String gameId, String playerName, Object moveData) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        Player currentPlayer = game.getCurrentPlayer();
        if (!currentPlayer.getName().equals(playerName)) {
            throw new RuntimeException("Not your turn");
        }
        // Обработка хода (здесь можно добавить логику для фаз, добавления свойств и т.д.)
        game.nextPlayer();
        game = gameRepository.save(game);
        gameWebSocketController.broadcastGameState(game.getId());
        return game;
    }

    @Transactional
    public Game endPhase(String gameId, String playerName) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        if (!game.getCurrentPlayer().getName().equals(playerName)) {
            throw new RuntimeException("Not your turn to end phase");
        }
        game.endPhase();
        game = gameRepository.save(game);
        gameWebSocketController.broadcastGameState(game.getId());
        return game;
    }

    public Game getGameState(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
    }
}
