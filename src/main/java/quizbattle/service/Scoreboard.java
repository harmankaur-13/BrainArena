package quizbattle.service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Live game scoreboard.
 */
public class Scoreboard {
    private final Map<String, Integer> scores = new LinkedHashMap<>();

    public void addPoints(String playerName, int points) {
        scores.put(playerName, scores.getOrDefault(playerName, 0) + points);
    }

    public int getScore(String playerName) {
        return scores.getOrDefault(playerName, 0);
    }

    /**
     * Prints live standings.
     */
    public void printStandings() {
        System.out.println("\n--- Current Standings ---");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            System.out.printf("  %d. %-15s %3d pts%n", rank++, entry.getKey(), entry.getValue());
        }
        System.out.println("-------------------------");
    }
}
