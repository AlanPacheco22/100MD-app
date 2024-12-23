package com.AlanPacheco.CienMD_app.Controller;


import com.AlanPacheco.CienMD_app.Entity.Game;
import com.AlanPacheco.CienMD_app.Repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameRepository gameRepository;

    // Crear un nuevo juego
    @PostMapping
    public Game createGame() {
        Game game = new Game();
        game.setDate(LocalDateTime.now());
        return gameRepository.save(game);
    }

    // Obtener todos los juegos
    @GetMapping
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    // Obtener un juego por ID
    @GetMapping("/{id}")
    public Game getGameById(@PathVariable Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Juego no encontrado con ID: " + id));
    }
}