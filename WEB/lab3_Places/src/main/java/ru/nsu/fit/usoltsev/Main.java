package ru.nsu.fit.usoltsev;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Insert place name: ");
        String placeName = scanner.nextLine();

        System.out.println("Insert count of interesting places, which you want to see: ");
        int limit = scanner.nextInt();
        while (limit <= 0) {
            System.out.println("Insert correct (>0) count of interesting places: ");
            limit = scanner.nextInt();
        }

        System.out.println("Insert radius where interesting places will be searched: ");
        int radius = scanner.nextInt();
        while (radius <= 0) {
            System.out.println("Insert correct (>0) radius: ");
            radius = scanner.nextInt();
        }

        LocationFinder locationFinder = new LocationFinder(placeName, limit, radius);
        locationFinder.start();

        scanner.close();
    }
}