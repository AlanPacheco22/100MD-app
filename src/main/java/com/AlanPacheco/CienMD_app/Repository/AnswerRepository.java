package com.AlanPacheco.CienMD_app.Repository;

import com.AlanPacheco.CienMD_app.Entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Answer findByQuestionIdAndTextIgnoreCase(Long questionId, String text);
}
