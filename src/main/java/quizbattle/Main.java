package quizbattle;

import quizbattle.dao.DBConnection;
import quizbattle.dao.QuestionDAO;
import quizbattle.dao.ScoreDAO;
import quizbattle.model.BotPlayer;
import quizbattle.model.HumanPlayer;
import quizbattle.model.Player;
import quizbattle.model.Question;
import quizbattle.service.QuizEngine;
import quizbattle.util.ConsoleUI;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main application entry point for the Quiz Battle CLI.
 */
public class Main {
    private static final QuestionDAO questionDAO = new QuestionDAO();
    private static final ScoreDAO scoreDAO = new ScoreDAO();
    private static final QuizEngine engine = new QuizEngine();

    public static void main(String[] args) {
        // Initialize database tables and seed questions on startup
        DBConnection.initDatabase();

        System.out.println("=== MULTIPLAYER QUIZ BATTLE (TERMINAL) ===");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = ConsoleUI.promptInt("Select an option", 0, 6);
            switch (choice) {
                case 1 -> playGame();
                case 2 -> scoreDAO.printTopPlayers(5);
                case 3 -> showPlayerStats();
                case 4 -> addQuestion();
                case 5 -> viewOrDeleteQuestions();
                case 6 -> runConcurrencyDemo();
                case 0 -> { System.out.println("Goodbye!"); running = false; }
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n--- MAIN MENU ---\n1. Play a game\n2. Leaderboard (Top 5)\n3. Player stats\n4. Add a question\n5. View/delete questions\n6. Concurrency demo (Safe vs Unsafe)\n0. Exit");
    }

    private static void playGame() {
        String name = ConsoleUI.prompt("Enter your player name:");
        if (name.isEmpty()) name = "Player";
        int count = ConsoleUI.promptInt("Number of questions to play (1 to 20)", 1, 20);

        List<Question> questions = questionDAO.findRandom(count);
        if (questions.isEmpty()) {
            System.out.println("No questions available. Please add some first.");
            return;
        }

        // 1 Human + 2 Bots (GeniusBot: 0.90 accuracy, RookieBot: 0.40 accuracy, 2-5s delay)
        List<Player> players = List.of(
                new HumanPlayer(name),
                new BotPlayer("GeniusBot", 0.90, 2000, 4000),
                new BotPlayer("RookieBot", 0.40, 2500, 5000)
        );

        engine.runGame(players, questions);
    }

    private static void showPlayerStats() {
        String name = ConsoleUI.prompt("Enter player name:");
        if (!name.isEmpty()) scoreDAO.printStatsFor(name);
    }

    private static void addQuestion() {
        System.out.println("\n--- Add a New Question ---");
        String text = ConsoleUI.prompt("Question text:");
        String a = ConsoleUI.prompt("Option A:"), b = ConsoleUI.prompt("Option B:");
        String c = ConsoleUI.prompt("Option C:"), d = ConsoleUI.prompt("Option D:");
        String correct = ConsoleUI.prompt("Correct option (A/B/C/D):").toUpperCase();
        int correctIdx = "ABCD".indexOf(correct.isEmpty() ? "A" : correct.substring(0, 1));
        if (correctIdx < 0) correctIdx = 0;

        String category = ConsoleUI.prompt("Category (e.g. Science, History):");
        if (category.isEmpty()) category = "General";

        questionDAO.insert(new Question(0, text, List.of(a, b, c, d), correctIdx, category));
        System.out.println("Question added successfully!");
    }

    private static void viewOrDeleteQuestions() {
        List<Question> list = questionDAO.findAll();
        System.out.println("\n--- Question Bank ---");
        for (Question q : list) {
            System.out.printf("[%d] (%s) %s -> %c%n", q.getId(), q.getCategory(), q.getText(), q.getCorrectOptionLetter());
        }
        int id = ConsoleUI.promptInt("Enter question ID to delete (0 to cancel)", 0, 100000);
        if (id > 0) {
            System.out.println(questionDAO.delete(id) ? "Question deleted." : "ID not found.");
        }
    }

    /**
     * Viva demonstration: 20 threads simultaneously incrementing 1,000 times.
     * Shows how plain int++ loses updates from race conditions while AtomicInteger reaches 20,000.
     */
    private static void runConcurrencyDemo() {
        System.out.println("\n=== CONCURRENCY DEMO: SAFE VS UNSAFE COUNTER ===");
        System.out.println("Simulating 20 threads, each incrementing 1,000 times (Expected: 20000).\n");

        final int[] unsafeCounter = {0};
        final AtomicInteger safeCounter = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1), doneLatch = new CountDownLatch(20);

        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // Release all 20 threads simultaneously
                    for (int j = 0; j < 1000; j++) {
                        unsafeCounter[0]++;                // Unsafe int++ (loses updates)
                        safeCounter.incrementAndGet();     // Safe AtomicInteger (lock-free CAS)
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // Fire start signal
        try { doneLatch.await(); } catch (InterruptedException ignored) {}

        System.out.println("Expected Total:             20000");
        System.out.println("Unsafe counter (int++):     " + unsafeCounter[0] + " (Lost " + (20000 - unsafeCounter[0]) + " updates!)");
        System.out.println("Safe counter (Atomic):      " + safeCounter.get() + " (100% thread-safe, 0 lost)\n");
    }
}
