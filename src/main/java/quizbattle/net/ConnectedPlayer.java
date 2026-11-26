package quizbattle.net;

import quizbattle.model.Player;
import quizbattle.model.Question;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Server-side representation of a connected player.
 */
public class ConnectedPlayer extends Player {
    private final Socket socket;
    private final PrintWriter out;

    private volatile int currentChosenIndex = -1;
    private volatile long currentAnswerTimeMs = 0;
    private volatile boolean answeredThisRound = false;

    public ConnectedPlayer(String name, Socket socket, PrintWriter out) {
        super(name);
        this.socket = socket;
        this.out = out;
    }

    public Socket getSocket() {
        return socket;
    }

    public synchronized void send(String message) {
        if (out != null) {
            out.println(message);
            out.flush();
        }
    }

    public void resetRound() {
        this.currentChosenIndex = -1;
        this.currentAnswerTimeMs = 0;
        this.answeredThisRound = false;
    }

    public void recordAnswer(int chosenIndex, long timeMs) {
        this.currentChosenIndex = chosenIndex;
        this.currentAnswerTimeMs = timeMs;
        this.answeredThisRound = true;
    }

    public boolean hasAnswered() {
        return answeredThisRound;
    }

    public int getCurrentChosenIndex() {
        return currentChosenIndex;
    }

    public long getCurrentAnswerTimeMs() {
        return currentAnswerTimeMs;
    }

    @Override
    public int chooseAnswer(Question q) {
        return currentChosenIndex;
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }
}
