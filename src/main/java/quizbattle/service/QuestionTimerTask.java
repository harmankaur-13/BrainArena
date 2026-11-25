package quizbattle.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Manages multithreaded per-question countdown and non-blocking user input.
 * Demonstrates: ExecutorService, Callable, Future, and timeout handling.
 */
public class QuestionTimerTask {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "quiz-input-worker");
        t.setDaemon(true);
        return t;
    });

    /**
     * Reads a line of user input with a maximum time limit in seconds.
     * Returns null if the user ran out of time.
     */
    public static String readLineWithTimeout(String promptMessage, int seconds) {
        System.out.print(promptMessage + " [⏱ " + seconds + "s]: ");
        System.out.flush();

        Callable<String> readTask = () -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            return br.readLine();
        };

        Future<String> future = EXECUTOR.submit(readTask);

        try {
            String input = future.get(seconds, TimeUnit.SECONDS);
            return input != null ? input.trim() : "";
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("\n⏰ TIME'S UP! You took longer than " + seconds + " seconds.");
            return null;
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }
}
