package quizbattle.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Common constants used across the BrainArena project.
 * Supports environment variables, system properties, and automatic .env file loading.
 */
public final class Constants {
        private Constants() {
        } // Prevent instantiation

    private static final Map<String, String> ENV_FILE_VARS = loadDotEnv();

    private static Map<String, String> loadDotEnv() {
        Map<String, String> map = new HashMap<>();
        File envFile = new File(".env");
        if (envFile.exists() && envFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        String key = line.substring(0, eqIdx).trim();
                        String val = line.substring(eqIdx + 1).trim();
                        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                            val = val.substring(1, val.length() - 1);
                        }
                        map.put(key, val);
                    }
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    private static String getEnvOrProp(String key, String defaultValue) {
        String val = System.getenv(key);
        if (val != null && !val.isEmpty()) return val;
        val = System.getProperty(key);
        if (val != null && !val.isEmpty()) return val;
        val = ENV_FILE_VARS.get(key);
        if (val != null && !val.isEmpty()) return val;
        return defaultValue;
    }

    public static String DB_HOST = getEnvOrProp("DB_HOST", "localhost");

    public static String getDbUrl() {
        String customUrl = getEnvOrProp("DB_URL", null);
        if (customUrl != null) {
            return customUrl;
        }
        return "jdbc:mysql://" + DB_HOST + ":3306/quizbattle?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false";
    }

    public static final String DB_URL = getDbUrl();
    public static final String DB_USER = getEnvOrProp("DB_USER", "root");
    public static final String DB_PASSWORD = getEnvOrProp("DB_PASSWORD", "");

        // Points awarded per correct answer
        public static final int POINTS_PER_CORRECT = 10;

        // Default time limit per question in seconds (multithreaded timer)
        public static final int QUESTION_TIME_LIMIT_SECONDS = 15;

        // ANSI Colors for console output
        public static final String RESET = "\u001B[0m";
        public static final String BOLD = "\u001B[1m";
        public static final String RED = "\u001B[31m";
        public static final String GREEN = "\u001B[32m";
}
