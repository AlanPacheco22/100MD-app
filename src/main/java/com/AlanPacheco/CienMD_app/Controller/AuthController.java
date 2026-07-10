package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.DTO.RegisterDTO;
import com.AlanPacheco.CienMD_app.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new RegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") RegisterDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.register(dto.getUsername(), dto.getEmail(), dto.getPassword(), dto.getFullName());
        } catch (RuntimeException e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }
}
