package quizbattle.util;

/**
 * Common constants used across the BrainArena project.
 */
public final class Constants {
    private Constants() {} // Prevent instantiation

    public static final String DB_URL = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:mysql://localhost:3306/quizbattle?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false";
    public static final String DB_USER = System.getenv("DB_USER") != null
            ? System.getenv("DB_USER")
            : "root";
    public static final String DB_PASSWORD = System.getenv("DB_PASSWORD") != null
            ? System.getenv("DB_PASSWORD")
            : "";

    // Points awarded per correct answer
    public static final int POINTS_PER_CORRECT = 10;

    // ANSI Colors for console output
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
}
