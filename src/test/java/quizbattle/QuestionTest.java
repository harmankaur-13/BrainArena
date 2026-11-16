package quizbattle;

import org.junit.jupiter.api.Test;
import quizbattle.model.Question;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionTest {

    @Test
    void testQuestionIsCorrect() {
        Question q = new Question(
                1,
                "What is the capital of France?",
                List.of("London", "Paris", "Berlin", "Madrid"),
                1, // Paris is at index 1
                "Geography"
        );

        assertEquals('B', q.getCorrectOptionLetter());
        assertTrue(q.isCorrect(1));
        assertFalse(q.isCorrect(0));
        assertFalse(q.isCorrect(2));
        assertFalse(q.isCorrect(3));
    }
}
