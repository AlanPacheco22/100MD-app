package com.AlanPacheco.CienMD_app.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameUpdateDTO {
    private String event;
    private GameDTO game;

    public GameUpdateDTO() {}

    public GameUpdateDTO(String event, GameDTO game) {
        this.event = event;
        this.game = game;
    }
}
