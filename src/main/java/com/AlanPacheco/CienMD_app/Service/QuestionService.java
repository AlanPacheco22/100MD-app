package com.AlanPacheco.CienMD_app.Service;

import com.AlanPacheco.CienMD_app.DTO.AnswerDTO;
import com.AlanPacheco.CienMD_app.DTO.QuestionDTO;
import com.AlanPacheco.CienMD_app.Entity.Answer;
import com.AlanPacheco.CienMD_app.Entity.Question;
import com.AlanPacheco.CienMD_app.Repository.AnswerRepository;
import com.AlanPacheco.CienMD_app.Repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public QuestionService(QuestionRepository questionRepository, AnswerRepository answerRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    public List<QuestionDTO> getAllQuestions() {
        return questionRepository.findAll().stream().map(this::toDTO).toList();
    }

    public QuestionDTO getQuestionById(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pregunta no encontrada: " + id));
        return toDTO(q);
    }

    @Transactional
    public QuestionDTO createQuestion(QuestionDTO dto) {
        Question q = new Question();
        q.setText(dto.getText());
        q = questionRepository.save(q);
        if (dto.getAnswers() != null) {
            for (AnswerDTO a : dto.getAnswers()) {
                if (a.getText() != null && !a.getText().isBlank()) {
                    Answer answer = new Answer();
                    answer.setQuestion(q);
                    answer.setText(a.getText());
                    answer.setScore(a.getScore());
                    answerRepository.save(answer);
                }
            }
        }
        return toDTO(questionRepository.findById(q.getId()).orElseThrow());
    }

    @Transactional
    public QuestionDTO updateQuestion(Long id, QuestionDTO dto) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pregunta no encontrada: " + id));
        q.setText(dto.getText());
        questionRepository.save(q);
        answerRepository.findByQuestionId(id).forEach(a -> answerRepository.delete(a));
        if (dto.getAnswers() != null) {
            for (AnswerDTO a : dto.getAnswers()) {
                if (a.getText() != null && !a.getText().isBlank()) {
                    Answer answer = new Answer();
                    answer.setQuestion(q);
                    answer.setText(a.getText());
                    answer.setScore(a.getScore());
                    answerRepository.save(answer);
                }
            }
        }
        return toDTO(questionRepository.findById(q.getId()).orElseThrow());
    }

    @Transactional
    public void deleteQuestion(Long id) {
        answerRepository.findByQuestionId(id).forEach(a -> answerRepository.delete(a));
        questionRepository.deleteById(id);
    }

    private QuestionDTO toDTO(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(q.getId());
        dto.setText(q.getText());
        dto.setAnswers(q.getAnswers().stream().map(a -> {
            AnswerDTO ad = new AnswerDTO();
            ad.setId(a.getId());
            ad.setText(a.getText());
            ad.setScore(a.getScore());
            return ad;
        }).toList());
        return dto;
    }
}
