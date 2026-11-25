package quizbattle.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Common constants used across the BrainArena project.
 */
public final class Constants {
    private Constants() {} // Prevent instantiation

    private static final Map<String, String> ENV = loadEnv();

    public static final String DB_USER = getEnv("DB_USER", "root");
    public static final String DB_PASSWORD = getEnv("DB_PASSWORD", "");
    public static final String DB_HOST = getEnv("DB_HOST", "localhost");
    public static final String DB_PORT = getEnv("DB_PORT", "3306");
    public static final String DB_NAME = getEnv("DB_NAME", "quizbattle");

    public static final String DB_URL = getEnv("DB_URL",
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
                    + "?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false");

    // Points awarded per correct answer
    public static final int POINTS_PER_CORRECT = 10;

    // ANSI Colors for console output
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";

    private static String getEnv(String key, String defaultValue) {
        String val = System.getenv(key);
        if (val != null && !val.isEmpty()) {
            return val;
        }
        val = ENV.get(key);
        if (val != null) {
            return val;
        }
        return defaultValue;
    }

    private static Map<String, String> loadEnv() {
        Map<String, String> envMap = new HashMap<>();
        File envFile = new File(".env");
        if (!envFile.exists()) {
            return envMap;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String value = line.substring(eqIdx + 1).trim();
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    envMap.put(key, value);
                }
            }
        } catch (IOException ignored) {
        }
        return envMap;
    }
}
