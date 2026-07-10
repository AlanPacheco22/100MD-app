package com.AlanPacheco.CienMD_app.Config;

public class GameEvent {

    private final String type;
    private final Long gameId;

    public GameEvent(String type, Long gameId) {
        this.type = type;
        this.gameId = gameId;
    }

    public String getType() {
        return type;
    }

    public Long getGameId() {
        return gameId;
    }
}
