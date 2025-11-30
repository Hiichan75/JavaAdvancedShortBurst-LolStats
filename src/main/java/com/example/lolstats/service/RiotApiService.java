package com.example.lolstats.service;

import com.example.lolstats.model.Match;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class RiotApiService {

    @Value("${riot.api.key}")
    private String apiKey;

    // For EUW: riot.region=europe  (regional routing)
    @Value("${riot.region}")
    private String region;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // -------------------------------------------------------------------------------------
    // STEP 1: Get PUUID for a Riot ID (gameName + tagLine)
    // Endpoint: /riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}
    // -------------------------------------------------------------------------------------
    public String getPuuidFromRiotId(String gameName, String tagLine) throws Exception {

        String url = String.format(
                "https://%s.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s",
                region,
                URLEncoder.encode(gameName, StandardCharsets.UTF_8),
                URLEncoder.encode(tagLine, StandardCharsets.UTF_8)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Riot-Token", apiKey)
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("Account lookup failed, status = " + response.statusCode());
            return null;    // caller will handle gracefully
        }

        JsonNode json = mapper.readTree(response.body());
        return json.get("puuid").asText();
    }

    // -------------------------------------------------------------------------------------
    // STEP 2: Get last X match IDs by PUUID (match-v5)
    // Endpoint: /lol/match/v5/matches/by-puuid/{puuid}/ids
    // -------------------------------------------------------------------------------------
    public List<String> getRecentMatchIds(String puuid, int count) throws Exception {

        if (puuid == null) {
            return List.of();
        }

        String url = String.format(
                "https://%s.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?count=%d",
                region,
                puuid,
                count
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Riot-Token", apiKey)
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("Match ID fetch failed, status = " + response.statusCode());
            return List.of();
        }

        JsonNode json = mapper.readTree(response.body());
        List<String> ids = new ArrayList<>();

        for (JsonNode n : json) {
            ids.add(n.asText());
        }

        return ids;
    }

    // -------------------------------------------------------------------------------------
    // STEP 3: Convert Riot matches → our Match entity
    // -------------------------------------------------------------------------------------
    public List<Match> fetchMatchesForRiotId(String gameName, String tagLine, int count) throws Exception {

        String puuid = getPuuidFromRiotId(gameName, tagLine);
        if (puuid == null) {
            return new ArrayList<>();
        }

        List<String> ids = getRecentMatchIds(puuid, count);
        List<Match> result = new ArrayList<>();

        for (String id : ids) {

            String url = String.format(
                    "https://%s.api.riotgames.com/lol/match/v5/matches/%s",
                    region,
                    id
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("X-Riot-Token", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("Match fetch failed, status = " + response.statusCode());
                continue;
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode info = root.get("info");
            JsonNode participants = info.get("participants");

            long endTimestamp = info.get("gameEndTimestamp").asLong();
            LocalDate date = Instant.ofEpochMilli(endTimestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            // Find this player's stats in the participants list
            for (JsonNode p : participants) {

                if (!p.get("puuid").asText().equals(puuid)) {
                    continue;
                }

                Match match = new Match();
                // store "gameName#tagLine" for display and filtering
                match.setSummonerName(gameName + "#" + tagLine);
                match.setChampion(p.get("championName").asText());
                match.setKills(p.get("kills").asInt());
                match.setDeaths(p.get("deaths").asInt());
                match.setAssists(p.get("assists").asInt());
                match.setWin(p.get("win").asBoolean());
                match.setMatchDate(date);

                result.add(match);
                break;
            }
        }

        return result;
    }
}
