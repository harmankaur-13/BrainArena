package quizbattle.model;

/**
 * Abstract base class for all quiz players.
 * Demonstrates: Abstraction and Polymorphism (subclasses define chooseAnswer).
 */
public abstract class Player {
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
     * Abstract method: Each player type decides how to choose an answer.
     * WHY: BotPlayer overrides this to sleep and answer probabilistically.
     */
    public abstract int chooseAnswer(Question q) throws InterruptedException;
}
