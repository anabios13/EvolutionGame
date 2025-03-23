package com.company.mod.controller;

import com.company.mod.model.Game;
import com.company.mod.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class GameViewController {

    @Autowired
    private GameService gameService;

    @GetMapping("/game/{gameId}")
    public String showGame(@PathVariable String gameId, Model model, Authentication authentication) {
        Game game = gameService.getGameState(gameId);
        model.addAttribute("gameId", gameId);
        model.addAttribute("player", authentication.getName());
        model.addAttribute("game", game);
        return "game";
    }
}
