package quizbattle;

import quizbattle.dao.DBConnection;
import quizbattle.dao.QuestionDAO;
import quizbattle.dao.ScoreDAO;
import quizbattle.model.HumanPlayer;
import quizbattle.model.Player;
import quizbattle.model.Question;
import quizbattle.service.QuizEngine;
import quizbattle.util.ConsoleUI;

import java.util.List;

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

        System.out.println("=== QUIZ BATTLE (TERMINAL) ===");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = ConsoleUI.promptInt("Select an option", 0, 5);
            switch (choice) {
                case 1 -> playGame();
                case 2 -> scoreDAO.printTopPlayers(5);
                case 3 -> showPlayerStats();
                case 4 -> addQuestion();
                case 5 -> viewOrDeleteQuestions();
                case 0 -> { System.out.println("Goodbye!"); running = false; }
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n--- MAIN MENU ---\n1. Play a game\n2. Leaderboard (Top 5)\n3. Player stats\n4. Add a question\n5. View/delete questions\n0. Exit");
    }

    private static void playGame() {
        String name = ConsoleUI.prompt("Enter your player name:");
        if (name.isEmpty()) name = "Player";

        // Prompt user for topic
        List<String> categories = questionDAO.findCategories();
        String selectedCategory = null;

        if (!categories.isEmpty()) {
            System.out.println("\n--- Select a Topic ---");
            System.out.println("0. All Topics (Random Mix)");
            for (int i = 0; i < categories.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, categories.get(i));
            }
            int choice = ConsoleUI.promptInt("Choose topic (0 for All)", 0, categories.size());
            if (choice > 0) {
                selectedCategory = categories.get(choice - 1);
            }
        }

        int count = ConsoleUI.promptInt("Number of questions to play (1 to 20)", 1, 20);

        List<Question> questions = (selectedCategory == null)
                ? questionDAO.findRandom(count)
                : questionDAO.findRandomByCategory(selectedCategory, count);

        if (questions.isEmpty()) {
            System.out.println("No questions available for this selection. Please add some first.");
            return;
        }

        Player player = new HumanPlayer(name);
        engine.runGame(player, questions);
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
}
