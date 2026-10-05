package main;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        
        Scanner scanner = new Scanner(System.in);

        System.out.println("""
                === Menu ===
                [1] Student
                [2] Provider
                [3] Office Staff
                """;);
            char choice = scanner.next().charAt(0);
    }
}