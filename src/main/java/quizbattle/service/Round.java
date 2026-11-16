package quizbattle.service;

import quizbattle.model.Answer;
import quizbattle.model.Question;
import quizbattle.util.Constants;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages concurrency and scoring for a single question round.
 * Demonstrates: CountDownLatch for synchronized start, and AtomicInteger for lock-free rank allocation.
 */
public class Round {
    private final Question question;
    private final CountDownLatch startLatch;
    private final AtomicInteger rankCounter;

    public Round(Question question) {
        this.question = question;
        this.startLatch = new CountDownLatch(1);
        this.rankCounter = new AtomicInteger(0);
    }

    public Question getQuestion() {
        return question;
    }

    public CountDownLatch getStartLatch() {
        return startLatch;
    }

    /**
     * Submits an answer and assigns a unique rank atomically.
     * WHY for Viva: AtomicInteger.incrementAndGet() guarantees each correct player gets a unique rank.
     *
     * @param answer the submitted answer
     * @return points earned (10 for 1st, 7 for 2nd, 5 for 3rd, 3 for later, 0 if wrong)
     */
    public int submit(Answer answer) {
        if (!answer.isCorrect()) {
            return 0;
        }

        // Atomic rank increment ensures thread safety without synchronized block
        int rank = rankCounter.incrementAndGet();
        return calculatePoints(rank);
    }

    /**
     * Simple rank-based scoring: faster correct answers earn more points.
     */
    public int calculatePoints(int rank) {
        return switch (rank) {
            case 1 -> Constants.POINTS_RANK_1;
            case 2 -> Constants.POINTS_RANK_2;
            case 3 -> Constants.POINTS_RANK_3;
            default -> Constants.POINTS_OTHER;
        };
    }
}
