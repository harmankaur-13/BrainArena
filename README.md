# BrainArena

BrainArena is a command-line quiz game written in Java. One human player competes against two computer-controlled players on the same set of multiple-choice questions. Every player runs on its own thread, and the fastest correct answer earns the most points. Questions and results are stored in a MySQL database through JDBC.

## Problem Statement

Basic quiz programs usually handle one player at a time, so they do not show what happens when several participants act at once. They also lose all results when the program exits. BrainArena addresses both gaps. It lets several players answer simultaneously, keeps the ranking correct under concurrent access, and stores questions and scores in a database.

## Objectives

1. Run all players concurrently, with every question starting for everyone at the same moment.
2. Ensure that no two players are ever given the same rank.
3. Store questions and scores using JDBC, saving each game as a single transaction.
4. Provide a command-line menu to play, view the leaderboard and player statistics, and manage questions.
5. Apply core object-oriented principles in the design.

**Scope:** one human and two bots, untimed rounds, console interface only.
**Out of scope:** graphical interface, network play, round time limits, multiple human players.

## Features

- Human player against two bots (GeniusBot and RookieBot)
- Simultaneous start for each question
- Rank-based scoring: 10, 7 and 5 points for the first three correct answers, 0 for a wrong answer
- Bot answers stay hidden until every player has answered
- Round results and live standings after each question
- Persistent leaderboard and per-player statistics
- Add, view and delete questions from the menu
- Demonstration of a race condition using an unsafe and a thread-safe counter

## Design Overview

| Area | Implementation |
|---|---|
| Object-oriented design | Abstract `Player` class with `BotPlayer` and `HumanPlayer` subclasses; immutable `Question` and `Answer` classes |
| Collections | `List` for questions and players, `ConcurrentHashMap` for the live scoreboard |
| Concurrency | `ExecutorService`, `CountDownLatch`, `AtomicInteger`, `Future` |
| Database | JDBC with MySQL, DAO classes, `PreparedStatement`, one transaction per game |
| Exception handling | Custom unchecked `DataAccessException` for database errors |

### Game flow

1. The engine creates a round and submits one task per player to a fixed thread pool.
2. Each player thread waits on a latch. The engine releases it once, so all players start together.
3. A correct answer receives its rank from an `AtomicInteger`, which removes the risk of duplicate ranks.
4. The engine waits on every player's `Future`, so the round ends only after all players have answered.
5. After the final question, all scores are written to the database in one transaction.

### Bot behaviour

The bots are not artificial intelligence. A bot waits for a random delay to simulate thinking, then selects the correct option with a fixed probability (its accuracy) and a random wrong option otherwise.

| Bot | Accuracy | Delay |
|---|---|---|
| GeniusBot | 90% | 2.0 to 4.0 seconds |
| RookieBot | 40% | 2.5 to 5.0 seconds |

## Project Structure

```
src/main/java/quizbattle/
    Main.java            Command-line menu
    model/               Question, Answer, Player, BotPlayer, HumanPlayer
    service/             QuizEngine, Round, Scoreboard
    dao/                 DBConnection, QuestionDAO, ScoreDAO
    util/                Constants, ConsoleUI
    exception/           DataAccessException
src/main/resources/      schema.sql, seed.sql
src/test/java/           QuestionTest, RoundConcurrencyTest
```