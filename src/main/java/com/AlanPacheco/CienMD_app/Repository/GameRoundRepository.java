package com.AlanPacheco.CienMD_app.Repository;

import com.AlanPacheco.CienMD_app.Entity.GameRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRoundRepository extends JpaRepository<GameRound, Long> {

    @Query("SELECT COALESCE(SUM(gr.score), 0) FROM GameRound gr WHERE gr.participant.id = :participantId")
    int sumScoreByParticipantId(@Param("participantId") Long participantId);

    @Query("SELECT gr FROM GameRound gr WHERE gr.gameQuestion.id = :gameQuestionId")
    List<GameRound> findByGameQuestionId(@Param("gameQuestionId") Long gameQuestionId);

    @Query("SELECT COALESCE(SUM(gr.score), 0) FROM GameRound gr WHERE gr.game.id = :gameId AND gr.participant.team = :team")
    int sumScoreByGameIdAndTeam(@Param("gameId") Long gameId, @Param("team") int team);
}
