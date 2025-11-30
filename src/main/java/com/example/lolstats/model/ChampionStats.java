package com.example.lolstats.model;

public class ChampionStats {

    private int games = 0;
    private int wins = 0;
    private int kills = 0;
    private int deaths = 0;
    private int assists = 0;

    public void addMatch(Match m) {
        games++;
        if (m.isWin()) {
            wins++;
        }
        kills += m.getKills();
        deaths += m.getDeaths();
        assists += m.getAssists();
    }


    public int getGames() {
        return games;
    }

    public int getWins() {
        return wins;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getAssists() {
        return assists;
    }


    public double getKda() {
        return deaths == 0
                ? (kills + assists)
                : (kills + assists) / (double) deaths;
    }


    public double getWinrate() {
        return games == 0 ? 0.0 : (wins * 100.0) / games;
    }


    public double getAvgKills() {
        return games == 0 ? 0.0 : kills / (double) games;
    }


    public double getAvgDeaths() {
        return games == 0 ? 0.0 : deaths / (double) games;
    }


    public double getAvgAssists() {
        return games == 0 ? 0.0 : assists / (double) games;
    }
}
