package com.example.lolstats.repository;

import com.example.lolstats.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    // Find all matches for a specific summoner (case-insensitive)
    List<Match> findBySummonerNameIgnoreCase(String summonerName);
}
