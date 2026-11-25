# BrainArena

BrainArena is a fast, interactive command-line quiz game written in pure Java (no build tools required). Players answer multiple-choice trivia questions, view instant feedback and live scores, and track their stats and rankings stored in a MySQL database through JDBC.

## Objectives

1. Deliver a responsive, barrier-free terminal quiz experience with immediate question flow.
2. Store questions, game records, and player statistics in MySQL using JDBC.
3. Provide a command-line menu to play, view top leaderboards, inspect individual statistics, and manage the question bank.
4. Apply core object-oriented principles (encapsulation, abstraction, inheritance, polymorphism) in the design.

**Scope:** Single-player interactive console interface, immediate feedback.  
**Out of scope:** Graphical interface, network play, artificial wait delays.

## Features

- Fast, barrier-free trivia gameplay with zero artificial wait delays
- Topic/category selection (Science, History, Technology, Geography, General Knowledge, or All Topics)
- Instant question evaluation and real-time score updates (+10 points per correct answer)
- Persistent leaderboard (Top 5 players by total score)
- Career statistics per player (games played, wins, total points, correct answers)
- Question bank management: Add new questions or view and delete existing ones
- Clean JDBC persistence with custom exception handling

## How to Run

1. Configure environment variables:
   Copy `.env.example` to `.env` and fill in your MySQL credentials (especially `DB_PASSWORD`):
   ```bash
   cp .env.example .env
   ```

2. Simply run the execution script:
   ```bash
   ./run.sh
   ```
   *(The script automatically loads `.env`, compiles code into `bin/`, syncs SQL resources, and starts the game without interactively prompting for a password).*

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
BrainArena/
├── run.sh               # Single script: compiles & starts the game
├── README.md
├── .gitignore
├── lib/
│   └── mysql-connector-j-26.10.0.jar
├── src/
│   └── main/
│       ├── java/quizbattle/
│       │   ├── Main.java
│       │   ├── dao/
│       │   ├── model/
│       │   ├── service/
│       │   ├── util/
│       │   └── exception/
│       └── resources/
│           ├── schema.sql
│           └── seed.sql (200 questions)
└── bin/                 # Generated build output (ignored by git)
```
