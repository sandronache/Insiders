package com.insiders.util;
import java.util.Scanner;

public class ConsoleIO {
    private static final Scanner sc = new Scanner(System.in);

    public static String readLine(String prompt){
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    public static int readInt(String prompt){
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            }
            catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            String input = readLine(prompt);
            if (InputValidator.isValidNumberInRange(input, min, max)) {
                return Integer.parseInt(input.trim());
            } else {
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            }
        }
    }
}