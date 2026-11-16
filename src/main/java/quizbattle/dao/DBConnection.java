package quizbattle.dao;

import quizbattle.exception.DataAccessException;
import quizbattle.util.Constants;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages database connections using standard JDBC DriverManager.
 * WHY: No connection pooling or complex reflection proxies; direct and student-friendly.
 */
public class DBConnection {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static Connection getConnection() throws SQLException {
        // Simple direct connection using SQLite JDBC
        return DriverManager.getConnection(Constants.DB_URL);
    }

    /**
     * Creates tables on startup and seeds 20 questions if the database is empty.
     */
    public static void initDatabase() {
        try (Connection conn = getConnection()) {
            executeScript(conn, "schema.sql");

            // Seed only if questions table has no records
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM questions")) {
                if (rs.next() && rs.getInt("total") == 0) {
                    executeScript(conn, "seed.sql");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to initialize database", e);
        }
    }

    private static void executeScript(Connection conn, String scriptName) {
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream(scriptName)) {
            if (is == null) return;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Statement stmt = conn.createStatement()) {
                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) continue;
                    sql.append(line).append(" ");
                    if (trimmed.endsWith(";")) {
                        stmt.execute(sql.toString());
                        sql.setLength(0);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Failed executing script: " + scriptName, e);
        }
    }
}
