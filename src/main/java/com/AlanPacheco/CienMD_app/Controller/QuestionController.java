package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.Entity.Question;
import com.AlanPacheco.CienMD_app.Repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionRepository questionRepository;

    // Obtener todas las preguntas
    @GetMapping
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    // Crear una pregunta con respuestas asociadas
    @PostMapping
    public Question createQuestion(@RequestBody Question question) {
        return questionRepository.save(question);
    }
}
