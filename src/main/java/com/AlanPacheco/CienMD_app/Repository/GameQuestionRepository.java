package com.AlanPacheco.CienMD_app.Repository;

import com.AlanPacheco.CienMD_app.Entity.GameQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameQuestionRepository extends JpaRepository<GameQuestion, Long> {
}
