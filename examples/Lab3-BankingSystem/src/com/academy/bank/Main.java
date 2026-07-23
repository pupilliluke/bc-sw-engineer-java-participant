package com.academy.bank;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BankService service = new BankService(scanner);
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> service.createCustomer();
                case "2" -> service.createSavingsAccount();
                case "3" -> service.createCurrentAccount();
                case "4" -> service.deposit();
                case "5" -> service.withdraw();
                case "6" -> service.displayAccounts();
                case "7" -> service.displayCustomers();
                case "8" -> {
                    System.out.println("Thank You");
                    running = false;
                }
                case "9" -> service.displayTransactions();
                default -> System.out.println("Invalid choice. Please enter 1-9.");
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== Academy Bank Menu =====");
        System.out.println("1. Create Customer");
        System.out.println("2. Create Savings Account");
        System.out.println("3. Create Current Account");
        System.out.println("4. Deposit");
        System.out.println("5. Withdraw");
        System.out.println("6. Display Accounts");
        System.out.println("7. Display Customers");
        System.out.println("8. Exit");
        System.out.println("9. Display Transactions   (bonus)");
        System.out.print("Enter choice: ");

//         Account a = new Account("X", 0, someCustomer); // must NOT compile
    }
}