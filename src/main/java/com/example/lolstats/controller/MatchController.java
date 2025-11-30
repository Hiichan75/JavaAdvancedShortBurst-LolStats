package com.example.lolstats.controller;

import com.example.lolstats.model.Match;
import com.example.lolstats.repository.MatchRepository;
import com.example.lolstats.service.RiotApiService;
import com.example.lolstats.model.ChampionStats;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;



@Controller
public class MatchController {

    private final MatchRepository matchRepository;
    private final RiotApiService riotApiService;

    public MatchController(MatchRepository matchRepository,
                           RiotApiService riotApiService) {
        this.matchRepository = matchRepository;
        this.riotApiService = riotApiService;
    }

    @GetMapping("/")
    public String redirectToMatches() {
        return "redirect:/matches";
    }

    // Show matches (optionally filtered by summoner string, e.g. "faker#GUD")
    @GetMapping("/matches")
    public String showMatches(
            @RequestParam(value = "summoner", required = false) String summoner,
            Model model) {

        List<Match> matches;

        if (summoner != null && !summoner.isBlank()) {
            matches = matchRepository.findBySummonerNameIgnoreCase(summoner);
        } else {
            matches = matchRepository.findAll();
        }

        long total = matches.size();
        long wins = matches.stream().filter(Match::isWin).count();
        long losses = total - wins;
        double winrate = total == 0 ? 0.0 : (wins * 100.0) / total;

        int totalKills = matches.stream().mapToInt(Match::getKills).sum();
        int totalDeaths = matches.stream().mapToInt(Match::getDeaths).sum();
        int totalAssists = matches.stream().mapToInt(Match::getAssists).sum();

        double avgKills   = total == 0 ? 0.0 : (double) totalKills / total;
        double avgDeaths  = total == 0 ? 0.0 : (double) totalDeaths / total;
        double avgAssists = total == 0 ? 0.0 : (double) totalAssists / total;

        double kda = totalDeaths == 0
                ? (totalKills + totalAssists)
                : (double) (totalKills + totalAssists) / totalDeaths;

        model.addAttribute("matches", matches);
        model.addAttribute("total", total);
        model.addAttribute("wins", wins);
        model.addAttribute("losses", losses);
        model.addAttribute("winrate", String.format("%.1f", winrate));
        model.addAttribute("currentSummoner", summoner == null ? "" : summoner);

        model.addAttribute("avgKills",   String.format("%.1f", avgKills));
        model.addAttribute("avgDeaths",  String.format("%.1f", avgDeaths));
        model.addAttribute("avgAssists", String.format("%.1f", avgAssists));
        model.addAttribute("kda",        String.format("%.2f", kda));



        // Champion stats (group matches by most played champion)


        Map<String, ChampionStats> championStats = new HashMap<>();
        for (Match m : matches) {
            championStats
                    .computeIfAbsent(m.getChampion(), k -> new ChampionStats())
                    .addMatch(m);
        }

        // Sort champions by number of games (descending)
        Map<String, ChampionStats> sortedChampionStats =
                championStats.entrySet()
                        .stream()
                        .sorted((a, b) ->
                                Integer.compare(
                                        b.getValue().getGames(),
                                        a.getValue().getGames()
                                ))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (a, b) -> a,
                                LinkedHashMap::new //tried hashmap but it destroys the sorted order
                        ));
        model.addAttribute("championStats", sortedChampionStats);

        return "matches";
    }

    // Fetch matches for a Riot ID (gameName + tagLine) and redirect to that summoner's page
    @PostMapping("/matches/fetch")
    public String fetchFromRiot(@RequestParam("gameName") String gameName,
                                @RequestParam("tagLine") String tagLine) {
        try {
            // how many matches u can see (last 20)
            var matches = riotApiService.fetchMatchesForRiotId(gameName, tagLine, 20);
            matchRepository.saveAll(matches);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String riotId = gameName + "#" + tagLine;
        String encoded = URLEncoder.encode(riotId, StandardCharsets.UTF_8);
        return "redirect:/matches?summoner=" + encoded;
    }


}
