package quizbattle.service;

import quizbattle.model.Answer;
import quizbattle.model.Question;
import quizbattle.util.Constants;

/**
 * Manages scoring for a single question round.
 */
public class Round {
    private final Question question;

    public Round(Question question) {
        this.question = question;
    }

    public Question getQuestion() {
        return question;
    }

    /**
     * Submits an answer and returns points earned.
     * Awards a speed bonus for answering quickly:
     * - Under 3s: +5 bonus points (15 pts total)
     * - Under 6s: +3 bonus points (13 pts total)
     * - Under 10s: +1 bonus point (11 pts total)
     * - 10s or more: 10 base points
     *
     * @param answer the submitted answer
     * @return points earned (base + speed bonus if correct, 0 if wrong)
     */
    public int submit(Answer answer) {
        if (!answer.isCorrect()) {
            return 0;
        }
        int points = Constants.POINTS_PER_CORRECT;
        long ms = answer.getTimeMs();
        if (ms < 3000) {
            points += 5;
        } else if (ms < 6000) {
            points += 3;
        } else if (ms < 10000) {
            points += 1;
        }
        return points;
    }
}
