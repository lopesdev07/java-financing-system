package util;

import java.util.Scanner;

public class LoopUtil {

    public static int getValidOption(Scanner scanner, int option, int min, int max) {
        while (option < min || option > max) {
            System.out.println("Error: Invalid input. Please enter a valid option.");
            option = ScannerUtil.intScanner(scanner);
        }
        return option;
    }
}