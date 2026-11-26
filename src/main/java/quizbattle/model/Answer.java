package quizbattle.model;

/**
 * Value object recording a player's answer and time taken.
 */
public class Answer {
    private final String playerName;
    private final int chosenIndex;
    private final long timeMs;
    private final boolean correct;
    private int points;

    public Answer(String playerName, int chosenIndex, long timeMs, boolean correct) {
        this.playerName = playerName;
        this.chosenIndex = chosenIndex;
        this.timeMs = timeMs;
        this.correct = correct;
        this.points = 0;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getChosenIndex() {
        return chosenIndex;
    }

    public char getChosenLetter() {
        if (chosenIndex >= 0 && chosenIndex <= 3) {
            return (char) ('A' + chosenIndex);
        }
        return '-';
    }

    public long getTimeMs() {
        return timeMs;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
