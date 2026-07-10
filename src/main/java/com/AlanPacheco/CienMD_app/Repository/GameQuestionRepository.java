package com.AlanPacheco.CienMD_app.Repository;

import com.AlanPacheco.CienMD_app.Entity.GameQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface GameQuestionRepository extends JpaRepository<GameQuestion, Long> {
    List<GameQuestion> findByGameId(Long gameId);

    @Query("SELECT gq.question.id FROM GameQuestion gq " +
            "WHERE DATE(gq.game.date) = :date")
    List<Long> findQuestionIdsUsedOnDate(LocalDate date);
}
