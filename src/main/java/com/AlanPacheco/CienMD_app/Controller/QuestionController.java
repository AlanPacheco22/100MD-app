package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.DTO.AnswerDTO;
import com.AlanPacheco.CienMD_app.DTO.QuestionDTO;
import com.AlanPacheco.CienMD_app.Entity.Answer;
import com.AlanPacheco.CienMD_app.Entity.Question;
import com.AlanPacheco.CienMD_app.Repository.QuestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionRepository questionRepository;

    public QuestionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> getAllQuestions() {
        List<QuestionDTO> dtos = questionRepository.findAll().stream()
                .map(q -> {
                    QuestionDTO dto = new QuestionDTO();
                    dto.setId(q.getId());
                    dto.setText(q.getText());
                    List<AnswerDTO> answerDTOs = q.getAnswers().stream()
                            .map(a -> {
                                AnswerDTO ad = new AnswerDTO();
                                ad.setId(a.getId());
                                ad.setText(a.getText());
                                ad.setScore(a.getScore());
                                return ad;
                            })
                            .toList();
                    dto.setAnswers(answerDTOs);
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<QuestionDTO> createQuestion(@RequestBody Question question) {
        question = questionRepository.save(question);
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setText(question.getText());
        List<AnswerDTO> answerDTOs = question.getAnswers().stream()
                .map(a -> {
                    AnswerDTO ad = new AnswerDTO();
                    ad.setId(a.getId());
                    ad.setText(a.getText());
                    ad.setScore(a.getScore());
                    return ad;
                })
                .toList();
        dto.setAnswers(answerDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
