package quizbattle.model;

import java.util.List;

/**
 * Represents a single quiz question with 4 multiple-choice options.
 * Demonstrates: Encapsulation (private fields, public getters).
 */
public class Question {
    private final int id;
    private final String text;
    private final List<String> options;
    private final int correctIndex; // 0 for A, 1 for B, 2 for C, 3 for D
    private final String category;

    public Question(int id, String text, List<String> options, int correctIndex, String category) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public char getCorrectOptionLetter() {
        return (char) ('A' + correctIndex);
    }

    public String getCategory() {
        return category;
    }

    /**
     * Checks if the chosen option index matches the correct answer.
     */
    public boolean isCorrect(int chosenIndex) {
        return chosenIndex == correctIndex;
    }
}
