package quizbattle.model;

import quizbattle.service.QuestionTimerTask;
import quizbattle.util.Constants;

/**
 * Represents a quiz player in the game.
 * Encapsulates player details, score tracking, and answer selection.
 */
public class Player {
    private final String name;
    private int score;
    private int correctCount;

    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.correctCount = 0;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void incrementCorrect() {
        this.correctCount++;
    }

    public void reset() {
        this.score = 0;
        this.correctCount = 0;
    }

    /**
     * Prompts the player to choose an answer under a countdown timer.
     * Returns the chosen option index (0 for A, 1 for B, 2 for C, 3 for D), or -1 if timed out.
     */
    public int chooseAnswer(Question q) {
        while (true) {
            String input = QuestionTimerTask.readLineWithTimeout(
                    "Your answer (A/B/C/D)", Constants.QUESTION_TIME_LIMIT_SECONDS);
            if (input == null) {
                // Timeout occurred
                return -1;
            }
            input = input.toUpperCase();
            if (input.length() == 1) {
                int index = "ABCD".indexOf(input.charAt(0));
                if (index >= 0) {
                    return index;
                }
            }
            System.out.println("Invalid option. Please enter A, B, C, or D.");
        }
    }
}
