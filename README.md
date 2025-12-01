# LoLStats – League of Legends Match Tracker

LolStats is a web application built with Java Spring Boot, Thymeleaf, and the Riot REST API.

It allows users to enter a Riot ID (e.g., faker#GUD) and fetch the last 20 League of Legends matches.

## The app displays:

Match history

KDA, winrate, champion stats

Most played champions

Summary of last 20 games

## The goal of the project was to learn:

How to interact with RIOT REST API

How to build a backend with Spring Boot

How to create a UI with Thymeleaf

How to structure a real-world full-stack application


## Technologies

- Java 17
- Spring Boot
- Spring MVC (controllers)
- H2 Database (store matches locally)
- Thymeleaf (HTML)
- Riot REST API

## How to Run the Project

- Add League of Legends matches:
  - summoner, champion, role, K/D/A, date, win/loss
- Store them in a database
- Show a table with all saved matches
- Calculate total games, wins, losses and winrate

## How to run

1. Clone the repository  
2. Add your Riot API key to application.properties (https://developer.riotgames.com/)
3. Run LolStatsApplication.java
4. Go to `http://localhost:8080/` in your browser

## Sources

https://developer.riotgames.com/
https://developer.riotgames.com/apis#account-v1
https://www.youtube.com/watch?v=0NycEiHOeX8&t=844s
https://chatgpt.com/
