package quizbattle.net;

import quizbattle.dao.ScoreDAO;
import quizbattle.model.Player;
import quizbattle.model.Question;
import quizbattle.util.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Universal Terminal Battle Server.
 * Supports direct connection from ANY terminal using standard tools:
 *   nc <HOST_IP> 5555  or  telnet <HOST_IP> 5555
 * Friends need ZERO code, ZERO Java, ZERO files installed!
 */
public class QuizServer implements Runnable {

    private final int port;
    private final String hostPlayerName;
    private final List<Question> questions;
    private final int timeLimitSec;
    private final String category;

    private ServerSocket serverSocket;
    private final List<ConnectedPlayer> players = new CopyOnWriteArrayList<>();
    private final AtomicBoolean gameStarted = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(true);

    private ConnectedPlayer hostPlayer;

    public QuizServer(int port, String hostPlayerName, List<Question> questions, int timeLimitSec, String category) {
        this.port = port;
        this.hostPlayerName = hostPlayerName;
        this.questions = questions;
        this.timeLimitSec = timeLimitSec;
        this.category = category;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        new Thread(this, "BrainArena-NetcatListener").start();
    }

    @Override
    public void run() {
        while (running.get() && !gameStarted.get()) {
            try {
                Socket socket = serverSocket.accept();
                if (gameStarted.get()) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                    out.println("\n[!] A game is already in progress. Please wait for the next match!");
                    socket.close();
                    continue;
                }
                new Thread(() -> handleTelnetClient(socket), "BrainArena-ClientHandshake").start();
            } catch (IOException e) {
                if (!running.get()) break;
            }
        }
    }

    private void handleTelnetClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            // Welcome banner for Telnet / Netcat users
            out.print("\033[2J\033[H"); // Clear screen in ANSI
            out.println(Constants.BOLD + "==================================================" + Constants.RESET);
            out.println(Constants.BOLD + "         🧠 WELCOME TO BRAINARENA BATTLE 🧠       " + Constants.RESET);
            out.println(Constants.BOLD + "==================================================" + Constants.RESET);
            out.print("Enter your player name: ");
            out.flush();

            String nameLine = in.readLine();
            if (nameLine == null) {
                socket.close();
                return;
            }
            nameLine = cleanInput(nameLine);
            if (nameLine.isEmpty()) nameLine = "Challenger";

            String uniqueName = makeUniqueName(nameLine);
            ConnectedPlayer player = new ConnectedPlayer(uniqueName, socket, out);
            players.add(player);

            out.println("\nWelcome, " + Constants.GREEN + uniqueName + Constants.RESET + "!");
            out.println("Connected to Host (" + hostPlayerName + ").");
            out.println("Topic: " + category + " | Questions: " + questions.size());
            out.println(Constants.BOLD + ">> Waiting for Host to start the battle... <<" + Constants.RESET);
            out.flush();

            broadcastLobbyNotice(">> " + Constants.GREEN + uniqueName + Constants.RESET + " joined the battle room! (Total players: " + (players.size() + (hostPlayer != null ? 1 : 0)) + ")");

            // Player listening loop for incoming answers
            String line;
            while ((line = in.readLine()) != null) {
                if (gameStarted.get()) {
                    line = cleanInput(line).toUpperCase();
                    if (line.length() >= 1 && "ABCD".contains(line.substring(0, 1))) {
                        int choiceIdx = "ABCD".indexOf(line.substring(0, 1));
                        if (!player.hasAnswered()) {
                            long timeTaken = System.currentTimeMillis() - currentQuestionStartTime;
                            player.recordAnswer(choiceIdx, timeTaken);
                            out.printf("%n  %s[✓] Answer %c buzzed in (%.1fs)! Waiting for other players...%s%n",
                                    Constants.GREEN, line.charAt(0), timeTaken / 1000.0, Constants.RESET);
                            out.flush();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            cleanupPlayer(socket);
        }
    }

    private static String cleanInput(String s) {
        if (s == null) return "";
        // Clean telnet control characters (\r, backspaces, etc.)
        return s.replaceAll("[\\r\\n]", "").trim();
    }

    private synchronized String makeUniqueName(String requested) {
        String name = requested;
        int counter = 2;
        boolean exists = true;
        while (exists) {
            exists = false;
            if (name.equalsIgnoreCase(hostPlayerName)) {
                name = requested + "_" + counter++;
                exists = true;
                continue;
            }
            for (ConnectedPlayer p : players) {
                if (p.getName().equalsIgnoreCase(name)) {
                    name = requested + "_" + counter++;
                    exists = true;
                    break;
                }
            }
        }
        return name;
    }

    private void cleanupPlayer(Socket socket) {
        ConnectedPlayer removed = null;
        for (ConnectedPlayer p : players) {
            if (p.getSocket() == socket) {
                removed = p;
                break;
            }
        }
        if (removed != null) {
            players.remove(removed);
            if (!gameStarted.get()) {
                broadcastLobbyNotice("<< " + removed.getName() + " left the room. (Remaining: " + (players.size() + 1) + ")");
            }
        }
    }

    private void broadcastLobbyNotice(String notice) {
        System.out.println(notice);
        for (ConnectedPlayer p : players) {
            p.send(notice);
        }
    }

    private volatile long currentQuestionStartTime = 0;

    /**
     * Runs the host interaction on the host's own console.
     */
    public void runHostConsole() {
        hostPlayer = new ConnectedPlayer(hostPlayerName, null, null);
        System.out.println("\nConnected Players:");
        System.out.printf("  1. %s (Host)%n", hostPlayerName);
        System.out.println(Constants.BOLD + "\n>> Press [ENTER] whenever you are ready to START the battle! <<" + Constants.RESET);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            br.readLine();
        } catch (IOException ignored) {}
        startGame();

        // Host's game loop interaction
        int roundNum = 1;
        int totalRounds = questions.size();

        for (Question q : questions) {
            runHostRound(roundNum++, totalRounds, q);
        }

        finishGame();
    }

    public synchronized void startGame() {
        gameStarted.set(true);
        String startBanner = "\n" + Constants.BOLD + "==================================================" + Constants.RESET +
                "\n" + Constants.BOLD + "             🏁 THE BATTLE HAS BEGUN! 🏁          " + Constants.RESET +
                "\n" + Constants.BOLD + "==================================================" + Constants.RESET +
                "\n  Topic:      " + category +
                "\n  Questions:  " + questions.size() +
                "\n  Time/Round: " + timeLimitSec + "s" +
                "\n  Scoring:    +10 pts for correct | ⚡ +5 FASTEST FINGER BONUS!" +
                "\n==================================================\n";

        System.out.println(startBanner);
        for (ConnectedPlayer p : players) {
            p.send(startBanner);
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
    }

    private void runHostRound(int roundNum, int totalRounds, Question q) {
        hostPlayer.resetRound();
        for (ConnectedPlayer p : players) {
            p.resetRound();
        }

        String qBanner = String.format("%n--------------------------------------------------%n" +
                        "%sQUESTION %d/%d | Category: %s | Time: %ds%s%n" +
                        "%s%s%s%n" +
                        "  A) %-25s B) %-25s%n" +
                        "  C) %-25s D) %-25s%n%n" +
                        "Type your answer (A/B/C/D) and hit ENTER: ",
                Constants.BOLD, roundNum, totalRounds, q.getCategory(), timeLimitSec, Constants.RESET,
                Constants.BOLD, q.getText(), Constants.RESET,
                q.getOptions().get(0), q.getOptions().get(1),
                q.getOptions().get(2), q.getOptions().get(3));

        System.out.print(qBanner);
        for (ConnectedPlayer p : players) {
            p.send(qBanner);
        }

        currentQuestionStartTime = System.currentTimeMillis();

        // Thread to read host input with timeout
        Thread hostInputThread = new Thread(() -> {
            try {
                BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
                while (!hostPlayer.hasAnswered() && System.currentTimeMillis() < currentQuestionStartTime + (timeLimitSec * 1000L)) {
                    if (console.ready()) {
                        String line = console.readLine();
                        if (line != null) {
                            line = cleanInput(line).toUpperCase();
                            if (line.length() >= 1 && "ABCD".contains(line.substring(0, 1))) {
                                int choiceIdx = "ABCD".indexOf(line.substring(0, 1));
                                long time = System.currentTimeMillis() - currentQuestionStartTime;
                                hostPlayer.recordAnswer(choiceIdx, time);
                                System.out.printf("%n  %s[✓] Answer %c buzzed in (%.1fs)! Waiting for other players...%s%n",
                                        Constants.GREEN, line.charAt(0), time / 1000.0, Constants.RESET);
                                break;
                            }
                        }
                    }
                    Thread.sleep(50);
                }
            } catch (Exception ignored) {}
        });
        hostInputThread.start();

        // Wait for all players to answer or timer to expire
        long deadline = currentQuestionStartTime + (timeLimitSec * 1000L);
        while (System.currentTimeMillis() < deadline) {
            boolean allAnswered = hostPlayer.hasAnswered();
            if (allAnswered) {
                for (ConnectedPlayer p : players) {
                    if (!p.hasAnswered()) {
                        allAnswered = false;
                        break;
                    }
                }
            }
            if (allAnswered) break;
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
                break;
            }
        }

        // Evaluate fastest finger
        List<Player> allParticipants = new ArrayList<>();
        allParticipants.add(hostPlayer);
        allParticipants.addAll(players);

        List<Player> correctPlayers = new ArrayList<>();
        for (Player p : allParticipants) {
            ConnectedPlayer cp = (ConnectedPlayer) p;
            if (cp.hasAnswered() && q.isCorrect(cp.getCurrentChosenIndex())) {
                correctPlayers.add(cp);
            }
        }
        correctPlayers.sort(Comparator.comparingLong(p -> ((ConnectedPlayer) p).getCurrentAnswerTimeMs()));

        for (int i = 0; i < correctPlayers.size(); i++) {
            ConnectedPlayer p = (ConnectedPlayer) correctPlayers.get(i);
            int speedBonus = (i == 0) ? 5 : (i == 1) ? 3 : (i == 2) ? 1 : 0;
            int roundPoints = Constants.POINTS_PER_CORRECT + speedBonus;
            p.addScore(roundPoints);
            p.incrementCorrect();
        }

        StringBuilder results = new StringBuilder();
        results.append("\n--------------------------------------------------\n");
        results.append(Constants.BOLD).append("                  ROUND RESULTS                   ").append(Constants.RESET).append("\n");
        results.append("--------------------------------------------------\n");
        results.append(String.format("Correct Answer: %c) %s\n\n", q.getCorrectOptionLetter(), q.getOptions().get(q.getCorrectIndex())));
        results.append("Performance:\n");

        for (Player p : allParticipants) {
            ConnectedPlayer cp = (ConnectedPlayer) p;
            if (!cp.hasAnswered()) {
                results.append(String.format("  • %-16s %s⏳ Timed out%s (0 pts)\n", cp.getName(), Constants.RED, Constants.RESET));
            } else if (q.isCorrect(cp.getCurrentChosenIndex())) {
                int rankIdx = correctPlayers.indexOf(cp);
                String bonus = rankIdx == 0 ? " ⚡ [FASTEST FINGER! +5 bonus]" :
                               rankIdx == 1 ? " ⚡ [+3 speed bonus]" :
                               rankIdx == 2 ? " ⚡ [+1 speed bonus]" : "";
                int pts = Constants.POINTS_PER_CORRECT + (rankIdx == 0 ? 5 : rankIdx == 1 ? 3 : rankIdx == 2 ? 1 : 0);
                results.append(String.format("  • %-16s %s✓ CORRECT%s (+%d pts, %.1fs)%s\n",
                        cp.getName(), Constants.GREEN, Constants.RESET, pts, cp.getCurrentAnswerTimeMs() / 1000.0, bonus));
            } else {
                char ch = cp.getCurrentChosenIndex() >= 0 ? (char) ('A' + cp.getCurrentChosenIndex()) : '?';
                results.append(String.format("  • %-16s %s✗ WRONG%s (Chose %c, 0 pts)\n", cp.getName(), Constants.RED, Constants.RESET, ch));
            }
        }

        allParticipants.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        results.append("\n--- Current Standings ---\n");
        int rank = 1;
        for (Player p : allParticipants) {
            results.append(String.format("  %d. %-16s %4d pts  (%d correct)\n", rank++, p.getName(), p.getScore(), p.getCorrectCount()));
        }
        results.append("-------------------------\n");

        String resStr = results.toString();
        System.out.println(resStr);
        for (ConnectedPlayer p : players) {
            p.send(resStr);
        }

        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {}
    }

    private void finishGame() {
        List<Player> allParticipants = new ArrayList<>();
        allParticipants.add(hostPlayer);
        allParticipants.addAll(players);

        allParticipants.sort((a, b) -> {
            int cmp = Integer.compare(b.getScore(), a.getScore());
            if (cmp != 0) return cmp;
            return Integer.compare(b.getCorrectCount(), a.getCorrectCount());
        });

        StringBuilder podium = new StringBuilder();
        podium.append("\n==================================================\n");
        podium.append("             🏆 FINAL BATTLE PODIUM 🏆            \n");
        podium.append("==================================================\n");

        List<Integer> ranks = new ArrayList<>();
        int currentRank = 1;
        for (int i = 0; i < allParticipants.size(); i++) {
            Player p = allParticipants.get(i);
            String medal = (i == 0) ? "🥇 1st Place" :
                           (i == 1) ? "🥈 2nd Place" :
                           (i == 2) ? "🥉 3rd Place" : String.format("   %dth Place", i + 1);

            podium.append(String.format("  %-12s: %-16s | %4d pts | %d/%d correct\n",
                    medal, p.getName(), p.getScore(), p.getCorrectCount(), questions.size()));
            ranks.add(currentRank++);
        }
        podium.append("==================================================\n");

        String finalStr = podium.toString();
        System.out.println(finalStr);
        for (ConnectedPlayer p : players) {
            p.send(finalStr);
        }

        try {
            new ScoreDAO().saveScores(allParticipants, ranks);
            String savedMsg = "\n[✓] Tournament results saved to Host MySQL database!\n";
            System.out.println(savedMsg);
            for (ConnectedPlayer p : players) {
                p.send(savedMsg);
            }
        } catch (Exception ignored) {}

        stop();
    }

    public void stop() {
        running.set(false);
        for (ConnectedPlayer p : players) {
            p.disconnect();
        }
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
    }
}
