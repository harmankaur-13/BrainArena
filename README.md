# BrainArena

BrainArena is an interactive command-line quiz game written in pure Java (no build tools required). It supports both single-player trivia and real-time fastest-finger LAN multiplayer battles across multiple computers over a local network.

## Objectives

1. Deliver a responsive, barrier-free terminal quiz experience with immediate question flow.
2. Provide real-time fastest-finger multiplayer over local network using standard socket connections.
3. Store questions, game records, and player statistics in MySQL using JDBC.
4. Provide a command-line menu to play, view top leaderboards, inspect individual statistics, and manage the question bank.
5. Apply core object-oriented principles (encapsulation, abstraction, inheritance, polymorphism) in the software design.

## Features

- **Single-Player and Multiplayer Modes:** Play solo or host real-time LAN battles.
- **Zero-Install Client for LAN Play:** Connected players only need standard terminal tools (`nc` or `telnet`) without installing Java or configuring databases.
- **Multithreaded Countdown Timer:** Real-time countdown per question managed via background executor threads.
- **Category Selection:** Science, History, Technology, Geography, General Knowledge, or All Topics.
- **Fastest-Finger Speed Bonus:** Awards bonus points for the fastest correct answer in multiplayer rounds.
- **Instant Evaluation & Scoring:** Immediate feedback with live score updates.
- **Persistent Leaderboard:** Top player rankings stored and updated in MySQL.
- **Career Statistics:** Tracks games played, wins, total points, and correct answers.
- **Question Bank Management:** Add, inspect, and remove questions from the database.
- **Clean JDBC Architecture:** Layered DAO structure with custom unchecked exception handling.

## How to Run

### Prerequisites

- Java Development Kit (JDK 8 or higher)
- MySQL Server

### 1. Environment Setup

Copy `.env.example` to `.env` and configure your database credentials:

```bash
cp .env.example .env
```

### 2. Start the Application

Execute the launch script:

```bash
./run.sh
```

The script automatically loads `.env`, compiles source files into `bin/`, synchronizes SQL scripts, and launches the application.

## Multiplayer Setup (LAN Battle)

### Host (Computer Running BrainArena)

1. Run `./run.sh` and select **Host Live Battle (Fastest Finger LAN)**.
2. Select topic, number of questions, and round timer.
3. The lobby screen will output the host Wi-Fi IP address and port (default: `5555`).
4. Once all players have joined, press **Enter** to start the match.

### Client Players (Connecting over Local Network)

Clients do not require Java or repository setup. On any computer connected to the same Wi-Fi network, players run:

```bash
nc <HOST_IP> 5555
```

*(On Windows, `telnet <HOST_IP> 5555` or `ncat` can be used).*

Players enter their display name and submit answers (`A`, `B`, `C`, or `D`) directly in their terminal.

## Design Overview

| Area | Implementation |
|---|---|
| Object-Oriented Design | Encapsulated `Player` base class; `ConnectedPlayer` specialization; immutable `Question` and `Answer` models |
| Networking | Multithreaded TCP server (`QuizServer`) accepting standard socket/Telnet/Netcat connections |
| Architecture | Layered DAO architecture (`DBConnection`, `QuestionDAO`, `ScoreDAO`) |
| Multithreading | Thread pools and concurrent collections (`CopyOnWriteArrayList`, `AtomicBoolean`, `ExecutorService`) |
| Database | JDBC with MySQL, `PreparedStatement`, transaction handling |
| Exception Handling | Custom unchecked `DataAccessException` for resilient persistence layer |

## Project Structure

```
BrainArena/
├── run.sh                  # Execution script (compiles & runs application)
├── README.md               # Documentation
├── .env.example            # Sample database environment configuration
├── .gitignore
├── lib/
│   └── mysql-connector-j-26.10.0.jar
├── src/
│   └── main/
│       ├── java/quizbattle/
│       │   ├── Main.java
│       │   ├── dao/
│       │   ├── model/
│       │   ├── net/
│       │   ├── service/
│       │   ├── util/
│       │   └── exception/
│       └── resources/
│           ├── schema.sql
│           └── seed.sql
└── bin/                    # Compiled classes (ignored by git)
```
