package quizbattle.dao;

import quizbattle.exception.DataAccessException;
import quizbattle.model.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object for game_scores table.
 * Demonstrates: ACID transactions with setAutoCommit(false), batch inserts, commit, and rollback.
 */
public class ScoreDAO {

    /**
     * Saves scores for all players in ONE database transaction.
     * WHY for Viva: Atomicity ensures all player scores are saved together or none at all.
     */
    public void saveScores(List<Player> players, List<Integer> finalRanks) {
        String sql = "INSERT INTO game_scores (player_name, score, correct_answers, final_rank) " +
                     "VALUES (?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 1. Start transaction

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < players.size(); i++) {
                    Player p = players.get(i);
                    int rank = finalRanks.get(i);

                    ps.setString(1, p.getName());
                    ps.setInt(2, p.getScore());
                    ps.setInt(3, p.getCorrectCount());
                    ps.setInt(4, rank);
                    ps.addBatch(); // Batch insert for performance
                }
                ps.executeBatch();
            }

            conn.commit(); // 2. Commit transaction if all succeeded
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // 3. Rollback on error to keep DB consistent
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw new DataAccessException("Failed to save scores in transaction", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Prints the all-time leaderboard directly to the console.
     */
    public void printTopPlayers(int limit) {
        String sql = "SELECT player_name, SUM(score) AS total, COUNT(*) AS games, " +
                     "       SUM(CASE WHEN final_rank = 1 THEN 1 ELSE 0 END) AS wins " +
                     "FROM game_scores GROUP BY player_name ORDER BY total DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-4s %-16s %-12s %-8s %-6s%n", "Rank", "Player", "Total Score", "Games", "Wins");
                System.out.println("--------------------------------------------------");
                int rank = 1;
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    System.out.printf("%-4d %-16s %-12d %-8d %-6d%n",
                            rank++, rs.getString("player_name"), rs.getInt("total"),
                            rs.getInt("games"), rs.getInt("wins"));
                }
                if (!hasData) {
                    System.out.println("No games recorded yet.");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch leaderboard", e);
        }
    }

    /**
     * Prints career statistics for a specific player name.
     */
    public void printStatsFor(String playerName) {
        String sql = "SELECT player_name, COUNT(*) AS games, " +
                     "       SUM(CASE WHEN final_rank = 1 THEN 1 ELSE 0 END) AS wins, " +
                     "       SUM(score) AS total_score, " +
                     "       SUM(correct_answers) AS total_correct " +
                     "FROM game_scores WHERE LOWER(player_name) = LOWER(?) GROUP BY player_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n--- Player Stats: " + rs.getString("player_name") + " ---");
                    System.out.println("  Games Played:    " + rs.getInt("games"));
                    System.out.println("  Total Wins:      " + rs.getInt("wins"));
                    System.out.println("  Total Points:    " + rs.getInt("total_score"));
                    System.out.println("  Correct Answers: " + rs.getInt("total_correct"));
                } else {
                    System.out.println("No stats found for player: " + playerName);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch player stats", e);
        }
    }
}
