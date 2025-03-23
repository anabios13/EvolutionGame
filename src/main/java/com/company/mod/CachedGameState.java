package com.company.mod;

import com.company.mod.model.Game;

public class CachedGameState {
    private final Game game;
    private final long timestamp;

    public CachedGameState(Game game, long timestamp) {
        this.game = game;
        this.timestamp = timestamp;
    }

    public Game getGame() {
        return game;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

