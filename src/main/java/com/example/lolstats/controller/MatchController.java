package com.example.lolstats.controller;

import com.example.lolstats.model.Match;
import com.example.lolstats.repository.MatchRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class MatchController {

    private final MatchRepository matchRepository;

    public MatchController(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @GetMapping("/")
    public String redirectToMatches() {
        return "redirect:/matches";
    }

    @GetMapping("/matches")
    public String showMatches(Model model) {
        List<Match> matches = matchRepository.findAll();

        long total = matches.size();
        long wins = matches.stream().filter(Match::isWin).count();
        long losses = total - wins;
        double winrate = total == 0 ? 0 : (wins * 100.0) / total;

        model.addAttribute("matches", matches);
        model.addAttribute("matchForm", new Match());
        model.addAttribute("total", total);
        model.addAttribute("wins", wins);
        model.addAttribute("losses", losses);
        model.addAttribute("winrate", String.format("%.1f", winrate));

        return "matches";
    }

    @PostMapping("/matches")
    public String addMatch(@ModelAttribute("matchForm") Match match) {
        if (match.getMatchDate() == null) {
            match.setMatchDate(LocalDate.now());
        }
        matchRepository.save(match);
        return "redirect:/matches";
    }

    @GetMapping("/matches/delete/{id}")
    public String deleteMatch(@PathVariable Long id) {
        matchRepository.deleteById(id);
        return "redirect:/matches";
    }
}
