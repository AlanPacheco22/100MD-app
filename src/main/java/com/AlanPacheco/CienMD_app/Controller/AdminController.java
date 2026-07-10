package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.DTO.AnswerDTO;
import com.AlanPacheco.CienMD_app.DTO.QuestionDTO;
import com.AlanPacheco.CienMD_app.Service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final QuestionService questionService;

    public AdminController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/questions")
    public String listQuestions(Model model) {
        model.addAttribute("questions", questionService.getAllQuestions());
        return "admin/questions";
    }

    @GetMapping("/questions/create")
    public String createForm(Model model) {
        QuestionDTO dto = new QuestionDTO();
        dto.setAnswers(new ArrayList<>());
        model.addAttribute("question", dto);
        return "admin/question-form";
    }

    @PostMapping("/questions/create")
    public String create(@Valid @ModelAttribute("question") QuestionDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/question-form";
        }
        questionService.createQuestion(dto);
        return "redirect:/admin/questions";
    }

    @GetMapping("/questions/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("question", questionService.getQuestionById(id));
        return "admin/question-form";
    }

    @PostMapping("/questions/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("question") QuestionDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/question-form";
        }
        questionService.updateQuestion(id, dto);
        return "redirect:/admin/questions";
    }

    @PostMapping("/questions/{id}/delete")
    public String delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return "redirect:/admin/questions";
    }
}
