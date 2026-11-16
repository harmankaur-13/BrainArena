package quizbattle.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Autonomous bot player with a customizable accuracy and reaction delay.
 * Demonstrates: Inheritance (extends Player) and Polymorphic method implementation.
 */
public class BotPlayer extends Player {
    private final double accuracy;  // Between 0.0 (always wrong) and 1.0 (always right)
    private final int minDelayMs;   // Minimum simulated thinking time
    private final int maxDelayMs;   // Maximum simulated thinking time

    public BotPlayer(String name, double accuracy, int minDelayMs, int maxDelayMs) {
        super(name);
        this.accuracy = accuracy;
        this.minDelayMs = Math.max(1000, minDelayMs);
        this.maxDelayMs = Math.max(this.minDelayMs, maxDelayMs);
    }

    public BotPlayer(String name, double accuracy) {
        this(name, accuracy, 2000, 5000); // Default 2 to 5 seconds
    }

    @Override
    public int chooseAnswer(Question q) throws InterruptedException {
        // Reaction delay between 2 and 5 seconds so humans have a fair chance
        int delay = ThreadLocalRandom.current().nextInt(minDelayMs, maxDelayMs + 1);
        Thread.sleep(delay);

        // 2. Decide if the bot answers correctly based on its accuracy
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll < accuracy) {
            return q.getCorrectIndex();
        }

        // 3. Otherwise pick a random incorrect option
        List<Integer> wrongIndices = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i != q.getCorrectIndex()) {
                wrongIndices.add(i);
            }
        }
        int pick = ThreadLocalRandom.current().nextInt(wrongIndices.size());
        return wrongIndices.get(pick);
    }
}
