package main.java.util;

public class Helper {
    // indexes
    public static int extractFirstLevel(String input) {
        if (input.length() == 1) {
            return Integer.parseInt(input);
        }
        int dot = input.indexOf('.');
        return Integer.parseInt(input.substring(0, dot));
    }

    public static String extractRemainingLevels(String input) {
        if (input.length() == 1) {
            return "";
        }
        int dot = input.indexOf('.');
        return input.substring(dot + 1);
    }

    public static boolean isCommentIdValid(String input) {
        return input.matches("(\\d\\.)*\\d");
    }

    // passwords

    public static boolean checkPassword(String password, int hash) {
        return hash == password.hashCode();
    }

    public static int hashFunction(String password) {
        return password.hashCode();
    }
}