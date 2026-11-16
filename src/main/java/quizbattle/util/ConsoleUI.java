package quizbattle.util;

import java.util.Scanner;

/**
 * Simple helper for reading user input from the console.
 */
public final class ConsoleUI {
    private static final Scanner SCANNER = new Scanner(System.in);

    private ConsoleUI() {}

    public static String prompt(String message) {
        System.out.print(message + " ");
        return SCANNER.nextLine().trim();
    }

    public static int promptInt(String message, int min, int max) {
        while (true) {
            System.out.print(message + ": ");
            String input = SCANNER.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
}
