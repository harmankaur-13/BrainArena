package quizbattle.service;

import quizbattle.dao.ScoreDAO;
import quizbattle.model.Player;
import quizbattle.model.Question;
import quizbattle.util.Constants;

import java.util.List;

/**
 * Core game loop managing questions, scoring, and database persistence.
 */
public class QuizEngine {
    private final ScoreDAO scoreDAO = new ScoreDAO();

    public void runGame(Player player, List<Question> questions) {
        player.reset();

        int questionNum = 1;
        int totalQuestions = questions.size();

        for (Question q : questions) {
            runQuestion(questionNum++, totalQuestions, q, player);
        }

        finishGame(player, totalQuestions);
    }

    private void runQuestion(int questionNum, int totalQuestions, Question q, Player player) {
        // 1. Print Question Banner
        System.out.printf("%n=== QUESTION %d/%d | Category: %s ===%n", questionNum, totalQuestions, q.getCategory());
        System.out.println(Constants.BOLD + q.getText() + Constants.RESET);
        System.out.printf("  A) %-25s B) %-25s%n", q.getOptions().get(0), q.getOptions().get(1));
        System.out.printf("  C) %-25s D) %-25s%n%n", q.getOptions().get(2), q.getOptions().get(3));

        // 2. Immediate player input under countdown timer
        long startTime = System.currentTimeMillis();
        int chosen = player.chooseAnswer(q);
        long elapsed = System.currentTimeMillis() - startTime;

        // 3. Evaluate answer and award points
        boolean correct = q.isCorrect(chosen);
        int points = correct ? Constants.POINTS_PER_CORRECT : 0;

        if (correct) {
            player.incrementCorrect();
            player.addScore(points);
        }

        // 4. Reveal result immediately
        char correctLetter = q.getCorrectOptionLetter();
        String correctText = q.getOptions().get(q.getCorrectIndex());

        if (correct) {
            System.out.printf("%n  %s✓ CORRECT!%s +%d pts (answered in %.1fs)%n",
                    Constants.GREEN, Constants.RESET, points, elapsed / 1000.0);
        } else if (chosen == -1) {
            System.out.printf("%n  %s⏱ TIMED OUT!%s +0 pts. The correct answer was %c) %s%n",
                    Constants.RED, Constants.RESET, correctLetter, correctText);
        } else {
            char chosenLetter = (char) ('A' + chosen);
            System.out.printf("%n  %s✗ WRONG!%s You chose %c. The correct answer was %c) %s%n",
                    Constants.RED, Constants.RESET, chosenLetter, correctLetter, correctText);
        }

        System.out.printf("  Current Score: %d pts | Correct: %d/%d%n",
                player.getScore(), player.getCorrectCount(), questionNum - 1);
    }

    private void finishGame(Player player, int totalQuestions) {
        // Save score to database
        scoreDAO.saveScores(List.of(player), List.of(1));

        System.out.println("\n=================================");
        System.out.println("        FINAL GAME RESULTS       ");
        System.out.println("=================================");
        System.out.printf("  Player:       %s%n", player.getName());
        System.out.printf("  Final Score:  %3d pts%n", player.getScore());
        System.out.printf("  Correct:      %d / %d (%.1f%%)%n",
                player.getCorrectCount(), totalQuestions,
                totalQuestions > 0 ? (player.getCorrectCount() * 100.0) / totalQuestions : 0.0);
        System.out.println("=================================");
        System.out.println("Score saved to database successfully!\n");
    }
}
