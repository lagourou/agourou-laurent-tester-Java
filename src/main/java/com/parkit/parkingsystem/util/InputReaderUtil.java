package com.parkit.parkingsystem.util;


import java.util.Scanner;

public class InputReaderUtil {

    private static final Scanner scan = new Scanner(System.in);

    public int readSelection() {
        try {
            return Integer.parseInt(scan.nextLine());
        } catch (Exception e) {
            System.err.println("Error while reading user input from Shell");
            System.err.println("Error reading input. Please enter valid number for proceeding further");
            return -1;
        }
    }

    public String readVehicleRegistrationNumber() {
        try {
            String vehicleRegNumber = scan.nextLine();
            if (vehicleRegNumber == null || vehicleRegNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid input provided");
            }
            return vehicleRegNumber;
        } catch (Exception e) {
            System.err.println("Error while reading user input from Shell");
            System.err.println("Error reading input. Please enter a valid string for vehicle registration number");
            throw e;
        }
    }

}
