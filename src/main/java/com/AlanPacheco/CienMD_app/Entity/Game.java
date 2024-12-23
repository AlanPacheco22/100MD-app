package com.AlanPacheco.CienMD_app.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<Participant> participants;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<GameQuestion> gameQuestions;

    // Constructor sin parámetros (requerido para JPA)
    public Game() {}

    // Getter para 'date'
    public LocalDateTime getDate() {
        return date;
    }

    // Setter para 'date'
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    // Otros getters y setters para otros campos si los necesitas
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public List<GameQuestion> getGameQuestions() {
        return gameQuestions;
    }

    public void setGameQuestions(List<GameQuestion> gameQuestions) {
        this.gameQuestions = gameQuestions;
    }
}

