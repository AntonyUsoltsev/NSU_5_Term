package ru.nsu.fit.usoltsev;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Insert place name: ");
        String placeName = scanner.nextLine();

        LocationFinder locationFinder = new LocationFinder(placeName);
        locationFinder.start();

        scanner.close();
    }
}