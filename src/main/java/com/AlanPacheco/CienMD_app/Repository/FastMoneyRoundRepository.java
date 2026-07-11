package com.AlanPacheco.CienMD_app.Repository;

import com.AlanPacheco.CienMD_app.Entity.FastMoneyRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FastMoneyRoundRepository extends JpaRepository<FastMoneyRound, Long> {

    List<FastMoneyRound> findByGameIdAndParticipantId(Long gameId, Long participantId);

    List<FastMoneyRound> findByGameIdAndPlayerNumber(Long gameId, int playerNumber);

    @Query("SELECT COALESCE(SUM(f.points), 0) FROM FastMoneyRound f WHERE f.game.id = :gameId AND f.participant.id = :participantId")
    int sumPointsByGameIdAndParticipantId(@Param("gameId") Long gameId, @Param("participantId") Long participantId);

    @Query("SELECT COALESCE(SUM(f.points), 0) FROM FastMoneyRound f WHERE f.game.id = :gameId")
    int sumAllPointsByGameId(@Param("gameId") Long gameId);

    @Query("SELECT f.answerText FROM FastMoneyRound f WHERE f.game.id = :gameId AND f.playerNumber = :playerNumber AND f.correct = true")
    List<String> findCorrectAnswersByGameIdAndPlayerNumber(@Param("gameId") Long gameId, @Param("playerNumber") int playerNumber);
}
