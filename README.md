# BrainArena

BrainArena is a fast, interactive command-line quiz game written in Java. Players answer multiple-choice trivia questions, view instant feedback and live scores, and track their stats and rankings stored in a MySQL database through JDBC.

## Objectives

1. Deliver a responsive, barrier-free terminal quiz experience with immediate question flow.
2. Store questions, game records, and player statistics in MySQL using JDBC.
3. Provide a command-line menu to play, view top leaderboards, inspect individual statistics, and manage the question bank.
4. Apply core object-oriented principles (encapsulation, abstraction, inheritance, polymorphism) in the design.

**Scope:** Single-player interactive console interface, immediate feedback.  
**Out of scope:** Graphical interface, network play, artificial wait delays.

## Features

- Fast, barrier-free trivia gameplay with zero artificial wait delays
- Instant question evaluation and real-time score updates (+10 points per correct answer)
- Persistent leaderboard (Top 5 players by total score)
- Career statistics per player (games played, wins, total points, correct answers)
- Question bank management: Add new questions or view and delete existing ones
- Clean JDBC persistence with custom exception handling

## Design Overview

| Area | Implementation |
|---|---|
| Object-oriented design | Abstract `Player` base class with `HumanPlayer` implementation; immutable `Question` and `Answer` classes |
| Collections | `List` for questions, `Map` for game score tracking |
| Architecture | Layered DAO architecture (`DBConnection`, `QuestionDAO`, `ScoreDAO`) |
| Database | JDBC with MySQL, `PreparedStatement`, transaction management |
| Exception handling | Custom unchecked `DataAccessException` for robust database error handling |

### Game Flow

1. The player enters their name, selects a topic/category (or chooses "All Topics" for a random mix), and picks the number of questions (1 to 20).
2. The engine fetches random questions matching the chosen topic from the database.
3. For each question, the player inputs their answer (A, B, C, or D).
4. The system validates the input immediately, awards points, and reveals the correct answer with instant feedback.
5. Upon game completion, results and statistics are saved directly to the MySQL database.

## Project Structure

```
src/main/java/quizbattle/
    Main.java            Command-line menu and game controller
    model/               Question, Answer, Player, HumanPlayer
    service/             QuizEngine, Round, Scoreboard
    dao/                 DBConnection, QuestionDAO, ScoreDAO
    util/                Constants, ConsoleUI
    exception/           DataAccessException
src/main/resources/      schema.sql, seed.sql (200 curated questions)
```
