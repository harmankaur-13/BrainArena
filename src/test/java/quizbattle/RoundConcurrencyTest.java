package quizbattle;

import org.junit.jupiter.api.Test;
import quizbattle.model.Answer;
import quizbattle.model.Question;
import quizbattle.service.Round;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundConcurrencyTest {

    @Test
    void testRoundGivesUniqueRanksUnder20Threads() throws InterruptedException {
        int threadCount = 20;
        Question q = new Question(1, "Sample Question", List.of("A", "B", "C", "D"), 0, "Science");
        Round round = new Round(q);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threadCount);

        List<Integer> pointsReceived = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final String name = "Bot-" + i;
            pool.submit(() -> {
                try {
                    startGate.await(); // All 20 threads wait here for the simultaneous start
                    Answer a = new Answer(name, 0, 100, true);
                    int pts = round.submit(a);
                    pointsReceived.add(pts);
                } catch (InterruptedException ignored) {
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown(); // Release all 20 threads at once
        assertTrue(doneGate.await(5, TimeUnit.SECONDS));
        pool.shutdown();

        // Exactly one player should get Rank 1 (10 pts), one Rank 2 (7 pts), one Rank 3 (5 pts), rest 3 pts
        long count10 = pointsReceived.stream().filter(p -> p == 10).count();
        long count7 = pointsReceived.stream().filter(p -> p == 7).count();
        long count5 = pointsReceived.stream().filter(p -> p == 5).count();
        long count3 = pointsReceived.stream().filter(p -> p == 3).count();

        assertEquals(1, count10, "Exactly one thread must get Rank 1 (10 points)");
        assertEquals(1, count7, "Exactly one thread must get Rank 2 (7 points)");
        assertEquals(1, count5, "Exactly one thread must get Rank 3 (5 points)");
        assertEquals(threadCount - 3, count3, "The remaining 17 threads must get 3 points");
    }
}
