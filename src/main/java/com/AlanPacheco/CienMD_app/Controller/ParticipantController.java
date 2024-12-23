package com.AlanPacheco.CienMD_app.Controller;


import com.AlanPacheco.CienMD_app.Entity.Participant;
import com.AlanPacheco.CienMD_app.Repository.GameRepository;
import com.AlanPacheco.CienMD_app.Repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games/{gameId}/participants")
public class ParticipantController {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private GameRepository gameRepository;

    // Agregar un participante a un juego
    @PostMapping
    public Participant addParticipant(@PathVariable Long gameId, @RequestBody Participant participant) {
        var game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Juego no encontrado con ID: " + gameId));
        participant.setGame(game);
        return participantRepository.save(participant);
    }

    // Obtener todos los participantes de un juego
    @GetMapping
    public List<Participant> getParticipants(@PathVariable Long gameId) {
        return (List<Participant>) participantRepository.findByGameId(gameId);
    }
}
