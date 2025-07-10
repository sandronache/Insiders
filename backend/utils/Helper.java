package utils;

import Services.UserService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Helper {
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

    public static void readUserData(UserService users) {
        try (BufferedReader reader = new BufferedReader(new FileReader(Constants.USERS_FILE_PATH))) {
            int count = 0;
            String[] oneUser = new String[3];
            String line;

            while ((line = reader.readLine()) != null) {
                oneUser[count] = line;
                count++;

                if (count == 3) {
                    users.addUser(oneUser[0], oneUser[1], Integer.parseInt(oneUser[2]));
                    count = 0;
                }
            }
        } catch (IOException e) {
            System.out.println("File reading error: " + e.getMessage());
        }
    }

    public static void writeUserData(UserService users) {
        try (FileWriter writer = new FileWriter(Constants.USERS_FILE_PATH)) {
            writer.write("Salut, Sebi!\n");
            writer.write("Acesta este un fișier text.\n");
            writer.write("Succes la programare! 🚀");
        } catch (IOException e) {
            System.out.println("File writing error: " + e.getMessage());
        }
    }
}
