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
     *
     * @param answer the submitted answer
     * @return points earned (POINTS_PER_CORRECT if correct, 0 if wrong)
     */
    public int submit(Answer answer) {
        if (!answer.isCorrect()) {
            return 0;
        }
        return Constants.POINTS_PER_CORRECT;
    }
}
