package quizbattle.util;

/**
 * Common constants used across the Quiz Battle project.
 * WHY: Centralizes settings without needing complex property file loaders.
 */
public final class Constants {
    private Constants() {} // Prevent instantiation

    public static final String DB_URL = "jdbc:sqlite:quizbattle.db";

    // Rank-based points
    public static final int POINTS_RANK_1 = 10;
    public static final int POINTS_RANK_2 = 7;
    public static final int POINTS_RANK_3 = 5;
    public static final int POINTS_OTHER = 3;

    // ANSI Colors for console output
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";
}
