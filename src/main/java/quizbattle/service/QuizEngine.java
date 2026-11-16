package quizbattle.service;

import quizbattle.dao.ScoreDAO;
import quizbattle.model.Answer;
import quizbattle.model.Player;
import quizbattle.model.Question;
import quizbattle.util.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Core game loop managing multithreaded rounds, thread coordination, and score persistence.
 * Demonstrates: ExecutorService, Future.get() synchronization, and synchronized console printing.
 */
public class QuizEngine {
    private final ScoreDAO scoreDAO = new ScoreDAO();

    public void runGame(List<Player> players, List<Question> questions) {
        for (Player p : players) p.reset();
        Scoreboard scoreboard = new Scoreboard();
        ExecutorService pool = Executors.newFixedThreadPool(players.size());

        try {
            int roundNum = 1;
            for (Question q : questions) {
                runRound(roundNum++, questions.size(), q, players, scoreboard, pool);
            }
        } finally {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(2, TimeUnit.SECONDS)) pool.shutdownNow();
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        finishGame(players);
    }

    private void runRound(int roundNum, int totalRounds, Question q, List<Player> players,
                          Scoreboard scoreboard, ExecutorService pool) {
        Round round = new Round(q);

        // Print Question Banner (no timer)
        System.out.printf("%n=== ROUND %d/%d | Category: %s ===%n", roundNum, totalRounds, q.getCategory());
        System.out.println(Constants.BOLD + q.getText() + Constants.RESET);
        System.out.printf("  A) %-20s B) %-20s%n", q.getOptions().get(0), q.getOptions().get(1));
        System.out.printf("  C) %-20s D) %-20s%n%n", q.getOptions().get(2), q.getOptions().get(3));

        // Submit each player task to the thread pool
        List<Future<Answer>> futures = new ArrayList<>();
        for (Player p : players) {
            Callable<Answer> task = () -> {
                // 1. Wait for start signal so all players begin simultaneously
                round.getStartLatch().await();

                // 2. Measure response time
                long startTime = System.currentTimeMillis();
                int chosen = p.chooseAnswer(q);
                long elapsed = System.currentTimeMillis() - startTime;

                // 3. Submit answer to round and award rank-based points
                boolean correct = q.isCorrect(chosen);
                Answer answer = new Answer(p.getName(), chosen, elapsed, correct);
                int points = round.submit(answer);
                answer.setPoints(points);

                if (correct) {
                    p.incrementCorrect();
                    p.addScore(points);
                    scoreboard.addPoints(p.getName(), points);
                }

                // 4. Live update without spoiling the chosen option for other players
                synchronized (System.out) {
                    System.out.printf("  [%s has answered!]%n", p.getName());
                }
                return answer;
            };
            futures.add(pool.submit(task));
        }

        // Release all players at the exact same instant!
        round.getStartLatch().countDown();

        // Wait for ALL players to finish by calling future.get() (no timeout)
        // WHY: future.get() blocks until that player's thread completes.
        // Calling future.get() on all futures guarantees the round ends only when EVERY player has answered.
        List<Answer> roundAnswers = new ArrayList<>();
        for (Future<Answer> f : futures) {
            try {
                roundAnswers.add(f.get());
            } catch (InterruptedException | ExecutionException ignored) {}
        }

        // Reveal correct answer and round results table now that everyone is done
        System.out.printf("%n  Correct Answer: %c) %s%n%n", q.getCorrectOptionLetter(), q.getOptions().get(q.getCorrectIndex()));
        System.out.println("--- Round Results ---");
        for (Answer a : roundAnswers) {
            String status = a.isCorrect() ? Constants.GREEN + "correct" + Constants.RESET : Constants.RED + "wrong" + Constants.RESET;
            String pts = a.getPoints() > 0 ? Constants.GREEN + "+" + a.getPoints() + Constants.RESET : "+0";
            System.out.printf("  %-12s -> %c  %-16s  %.1fs  %s%n",
                    a.getPlayerName(), a.getChosenLetter(), status, a.getTimeMs() / 1000.0, pts);
        }
        scoreboard.printStandings();

        try {
            Thread.sleep(1000); // Brief pause before next round
        } catch (InterruptedException ignored) {}
    }

    private void finishGame(List<Player> players) {
        List<Player> sorted = new ArrayList<>(players);
        sorted.sort(Comparator.comparingInt(Player::getScore).reversed());

        List<Integer> ranks = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) ranks.add(i + 1);

        scoreDAO.saveScores(sorted, ranks); // Save scores in one transaction

        System.out.println("\n=================================");
        System.out.println("        FINAL GAME RESULTS       ");
        System.out.println("=================================");
        for (int i = 0; i < sorted.size(); i++) {
            Player p = sorted.get(i);
            System.out.printf("  Rank %d: %-15s %3d pts (%d correct)%n",
                    i + 1, p.getName(), p.getScore(), p.getCorrectCount());
        }
        System.out.println("Winner: " + sorted.get(0).getName() + "! (Saved to DB)\n");
    }
}
