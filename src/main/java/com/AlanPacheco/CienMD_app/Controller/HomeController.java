package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.Service.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    private final GameService gameService;

    public HomeController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("games", gameService.getAllGames());
        return "dashboard";
    }

    @GetMapping("/jugar/{id}")
    public String playGame(@PathVariable Long id, Model model) {
        model.addAttribute("game", gameService.getGameById(id));
        return "play";
    }
}
