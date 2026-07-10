package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.Service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/history")
    public String history(Model model) {
        model.addAttribute("games", statsService.getGameHistory());
        return "history";
    }

    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("players", statsService.getPlayerStats());
        return "stats";
    }
}
