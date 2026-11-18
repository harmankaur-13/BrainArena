package quizbattle.model;

import quizbattle.util.ConsoleUI;

/**
 * Human player that reads answers interactively from the console.
 */
public class HumanPlayer extends Player {

    public HumanPlayer(String name) {
        super(name);
    }

    @Override
    public int chooseAnswer(Question q) {
        while (true) {
            String input = ConsoleUI.prompt("Your answer (A/B/C/D):").toUpperCase();
            if (input.length() == 1) {
                int index = "ABCD".indexOf(input.charAt(0));
                if (index >= 0) {
                    return index;
                }
            }
            System.out.println("Invalid option. Please enter A, B, C, or D.");
        }
    }
}
