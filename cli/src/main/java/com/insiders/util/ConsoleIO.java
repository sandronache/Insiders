package com.insiders.util;

import java.io.Console;
import java.util.Scanner;

public class ConsoleIO {
    private static final Scanner sc = new Scanner(System.in);

    public static String readLine(String prompt){
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    public static String readPassword(String prompt){
        Console c = System.console();
        if (c != null) {
            char[] pwd = c.readPassword(prompt);
            return pwd == null ? "" : new String(pwd);
        }
        System.out.print(prompt);
        return sc.nextLine();
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
}