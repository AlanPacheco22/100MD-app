package com.AlanPacheco.CienMD_app.Repository;

import com.AlanPacheco.CienMD_app.Entity.GameRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GameRoundRepository extends JpaRepository<GameRound, Long> {

    @Query("SELECT SUM(gr.score) FROM GameRound gr WHERE gr.participant.id = :participantId")
    int sumScoreByParticipantId(Long participantId);
}