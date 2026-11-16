package quizbattle.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe live scoreboard.
 * Demonstrates: ConcurrentHashMap for thread-safe concurrent score increments without locks.
 */
public class Scoreboard {
    private final ConcurrentHashMap<String, AtomicInteger> scores = new ConcurrentHashMap<>();

    public void addPoints(String playerName, int points) {
        // computeIfAbsent + addAndGet ensures thread-safe atomic addition
        scores.computeIfAbsent(playerName, k -> new AtomicInteger(0)).addAndGet(points);
    }

    public int getScore(String playerName) {
        AtomicInteger s = scores.get(playerName);
        return s != null ? s.get() : 0;
    }

    /**
     * Prints live standings sorted from highest to lowest score.
     */
    public void printStandings() {
        System.out.println("\n--- Current Standings ---");
        List<Map.Entry<String, AtomicInteger>> list = new ArrayList<>(scores.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue().get(), a.getValue().get()));

        int rank = 1;
        for (Map.Entry<String, AtomicInteger> entry : list) {
            System.out.printf("  %d. %-15s %3d pts%n", rank++, entry.getKey(), entry.getValue().get());
        }
        System.out.println("-------------------------");
    }
}
