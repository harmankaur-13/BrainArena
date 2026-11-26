package quizbattle;

import quizbattle.dao.DBConnection;
import quizbattle.dao.QuestionDAO;
import quizbattle.dao.ScoreDAO;
import quizbattle.model.Player;
import quizbattle.model.Question;
import quizbattle.net.NetworkUtils;
import quizbattle.net.QuestionProvider;
import quizbattle.net.QuizServer;
import quizbattle.service.QuizEngine;
import quizbattle.util.ConsoleUI;
import quizbattle.util.Constants;

import java.util.List;

/**
 * Main application entry point for the BrainArena CLI.
 */
public class Main {
    private static final QuestionDAO questionDAO = new QuestionDAO();
    private static final ScoreDAO scoreDAO = new ScoreDAO();
    private static final QuizEngine engine = new QuizEngine();

    public static void main(String[] args) {
        // Direct CLI flag execution
        if (args.length > 0) {
            String command = args[0].toLowerCase();
            if (command.equals("host") || command.equals("--host")) {
                hostBattleGame();
                return;
            }
        }

        // Initialize database if running host/solo
        try {
            DBConnection.initDatabase();
        } catch (Exception ignored) {}

        System.out.println(Constants.BOLD + "==================================================" + Constants.RESET);
        System.out.println(Constants.BOLD + "             🧠 WELCOME TO BRAINARENA 🧠          " + Constants.RESET);
        System.out.println(Constants.BOLD + "==================================================" + Constants.RESET);

        boolean running = true;
        while (running) {
            printMenu();
            int choice = ConsoleUI.promptInt("Select an option", 0, 6);
            switch (choice) {
                case 1 -> playSoloGame();
                case 2 -> hostBattleGame();
                case 3 -> showLeaderboard();
                case 4 -> showPlayerStats();
                case 5 -> addQuestion();
                case 6 -> viewOrDeleteQuestions();
                case 0 -> {
                    System.out.println("Thanks for playing BrainArena. Goodbye!");
                    running = false;
                }
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Play Solo (Practice Mode)");
        System.out.println(Constants.BOLD + "2. Host Live Battle (Fastest Finger LAN)" + Constants.RESET);
        System.out.println("3. Leaderboard (Top 5)");
        System.out.println("4. Player Stats");
        System.out.println("5. Add a Question");
        System.out.println("6. View/Delete Questions");
        System.out.println("0. Exit");
    }

    private static void playSoloGame() {
        String name = ConsoleUI.prompt("Enter your player name:");
        if (name.isEmpty()) name = "Player";

        List<String> categories = QuestionProvider.getAvailableCategories();
        String selectedCategory = null;

        if (!categories.isEmpty()) {
            System.out.println("\n--- Select a Topic ---");
            System.out.println("1. All Topics (Random Mix)");
            for (int i = 0; i < categories.size(); i++) {
                System.out.printf("%d. %s%n", i + 2, categories.get(i));
            }
            int choice = ConsoleUI.promptInt("Choose topic", 1, categories.size() + 1);
            if (choice > 1) {
                selectedCategory = categories.get(choice - 2);
            }
        }

        int count = ConsoleUI.promptInt("Number of questions to play (1 to 20)", 1, 20);
        List<Question> questions = QuestionProvider.getQuestions(selectedCategory, count);

        if (questions.isEmpty()) {
            System.out.println("No questions available for this selection.");
            return;
        }

        Player player = new Player(name);
        engine.runGame(player, questions);
    }

    private static void hostBattleGame() {
        System.out.println("\n" + Constants.BOLD + "--- Host a Live Fastest-Finger Battle ---" + Constants.RESET);
        String name = ConsoleUI.prompt("Enter your host player name:");
        if (name.isEmpty()) name = "HostPlayer";

        List<String> categories = QuestionProvider.getAvailableCategories();
        String selectedCategory = null;

        if (!categories.isEmpty()) {
            System.out.println("\nSelect Game Topic:");
            System.out.println("1. All Topics (Random Mix)");
            for (int i = 0; i < categories.size(); i++) {
                System.out.printf("%d. %s%n", i + 2, categories.get(i));
            }
            int choice = ConsoleUI.promptInt("Choose topic", 1, categories.size() + 1);
            if (choice > 1) {
                selectedCategory = categories.get(choice - 2);
            }
        }

        int count = ConsoleUI.promptInt("Number of questions (1 to 20)", 1, 20);
        int timeLimit = ConsoleUI.promptInt("Time limit per question in seconds (10 to 60)", 10, 60);

        List<Question> questions = QuestionProvider.getQuestions(selectedCategory, count);
        if (questions.isEmpty()) {
            System.out.println("No questions available for this selection.");
            return;
        }

        String primaryIp = NetworkUtils.getPrimaryLocalIP();
        int port = NetworkUtils.DEFAULT_PORT;

        System.out.println("\n" + Constants.BOLD + "============================================================" + Constants.RESET);
        System.out.println(Constants.BOLD + "         ⚡ LIVE FASTEST-FINGER MULTIPLAYER BATTLE ⚡       " + Constants.RESET);
        System.out.println(Constants.BOLD + "============================================================" + Constants.RESET);
        System.out.printf("  YOUR Wi-Fi IP:      %s%s%s%n", Constants.GREEN, primaryIp, Constants.RESET);
        System.out.printf("  Battle Port:        %d%n", port);
        System.out.printf("  Questions:          %d | Time limit: %ds per question%n", count, timeLimit);
        System.out.printf("  Topic:              %s%n", selectedCategory == null ? "All Topics" : selectedCategory);
        System.out.println("------------------------------------------------------------");
        System.out.println(Constants.BOLD + "  TELL YOUR FRIENDS ON WI-FI TO TYPE THIS IN THEIR TERMINAL:" + Constants.RESET);
        System.out.printf("    %snc %s %d%s%n", Constants.GREEN, primaryIp, port, Constants.RESET);
        System.out.printf("    (or: %stelnet %s %d%s)%n", Constants.GREEN, primaryIp, port, Constants.RESET);
        System.out.println("  They need NO Java, NO code, and NO files installed!");
        System.out.println(Constants.BOLD + "============================================================" + Constants.RESET);

        try {
            QuizServer server = new QuizServer(port, name, questions, timeLimit, selectedCategory == null ? "All" : selectedCategory);
            server.start();

            // Run host interaction right in this console
            server.runHostConsole();
        } catch (Exception e) {
            System.out.printf("%sFailed to start battle host: %s%s%n", Constants.RED, e.getMessage(), Constants.RESET);
        }
    }

    private static void showLeaderboard() {
        try {
            scoreDAO.printTopPlayers(5);
        } catch (Exception e) {
            System.out.println("Unable to load leaderboard. Ensure MySQL is running.");
        }
    }

    private static void showPlayerStats() {
        String name = ConsoleUI.prompt("Enter player name:");
        if (!name.isEmpty()) {
            try {
                scoreDAO.printStatsFor(name);
            } catch (Exception e) {
                System.out.println("Unable to load player stats. Ensure MySQL is running.");
            }
        }
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

        try {
            questionDAO.insert(new Question(0, text, List.of(a, b, c, d), correctIdx, category));
            System.out.println("Question added successfully!");
        } catch (Exception e) {
            System.out.println("Failed to insert question into MySQL.");
        }
    }

    private static void viewOrDeleteQuestions() {
        try {
            List<Question> list = questionDAO.findAll();
            System.out.println("\n--- Question Bank ---");
            for (Question q : list) {
                System.out.printf("[%d] (%s) %s -> %c%n", q.getId(), q.getCategory(), q.getText(), q.getCorrectOptionLetter());
            }
            int id = ConsoleUI.promptInt("Enter question ID to delete (0 to cancel)", 0, 100000);
            if (id > 0) {
                System.out.println(questionDAO.delete(id) ? "Question deleted." : "ID not found.");
            }
        } catch (Exception e) {
            System.out.println("Unable to view or delete questions. Ensure MySQL is running.");
        }
    }
}
