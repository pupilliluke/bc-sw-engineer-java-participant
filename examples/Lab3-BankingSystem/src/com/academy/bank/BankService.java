package com.academy.bank;

import java.util.Scanner;

public class BankService {
    private static final int MAX_CUSTOMERS = 50;
    private static final int MAX_ACCOUNTS = 100;
    private static final int MAX_TRANSACTIONS = 500;

    private final Customer[] customers = new Customer[MAX_CUSTOMERS];
    private final Account[] accounts = new Account[MAX_ACCOUNTS];
    private final Transaction[] transactions = new Transaction[MAX_TRANSACTIONS];

    private int customerCount = 0;
    private int accountCount = 0;
    private int transactionCount = 0;
    private int nextAccountNumber = 10001;
    private int nextTransactionNumber = 1;

    private final Scanner scanner;

    public BankService(Scanner scanner) {
        this.scanner = scanner;
    }

    // ---- Menu action stubs (implemented in later steps) ----
    public void createCustomer() {
        System.out.print("Enter Customer ID: ");
        String id = scanner.nextLine().trim();

        if (findCustomer(id) != null) {
            System.out.println("Customer ID already exists.");
            return;
        }
        if (customerCount >= MAX_CUSTOMERS) {
            System.out.println("Customer storage full.");
            return;
        }

        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine().trim();

        customers[customerCount++] = new Customer(id, name, email, phone);
        System.out.println("Customer Created Successfully.");
    }

    public void createSavingsAccount() {
        Customer customer = readExistingCustomer();
        if (customer == null) {
            return; // message already printed
        }

        double balance = readPositiveAmount("Enter Initial Balance: ");
        double rate = readPositiveAmount("Enter Interest Rate: ");

        if (accountCount >= MAX_ACCOUNTS) {
            System.out.println("Account storage full.");
            return;
        }

        String accountNumber = String.valueOf(nextAccountNumber++);
        SavingsAccount account = new SavingsAccount(accountNumber, balance, customer, rate);
        accounts[accountCount++] = account;

        System.out.println("Savings Account Created.");
        System.out.println("Account Number : " + accountNumber);
        System.out.println("Balance : " + formatAmount(balance));
        System.out.println("Interest Rate : " + formatAmount(rate) + "%");
    }

    public void createCurrentAccount() {
        Customer customer = readExistingCustomer();
        if (customer == null) {
            return;
        }

        double balance = readPositiveAmount("Enter Initial Balance: ");
        double fee = readPositiveAmount("Enter Transaction Fee: ");

        if (accountCount >= MAX_ACCOUNTS) {
            System.out.println("Account storage full.");
            return;
        }

        String accountNumber = String.valueOf(nextAccountNumber++);
        CurrentAccount account = new CurrentAccount(accountNumber, balance, customer, fee);
        accounts[accountCount++] = account;

        System.out.println("Current Account Created.");
        System.out.println("Account Number : " + accountNumber);
        System.out.println("Balance : " + formatAmount(balance));
        System.out.println("Transaction Fee : " + formatAmount(fee));
    }

    // ---- Helpers ----

    private Customer readExistingCustomer() {
        System.out.print("Enter Customer ID: ");
        String id = scanner.nextLine().trim();
        Customer customer = findCustomer(id);
        if (customer == null) {
            System.out.println("Customer not found. Create the customer first.");
        }
        return customer;
    }

    private double readPositiveAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("Value must be positive.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private String formatAmount(double value) {
        // Prints 10000 instead of 10000.0 when whole, matching the expected output
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    public void deposit() {
        Account account = readExistingAccount();
        if (account == null) {
            return;
        }

        double amount = readPositiveAmount("Enter Deposit Amount: ");
        account.deposit(amount);
        recordTransaction(account, amount, "DEPOSIT");
        System.out.println("Balance Updated : " + formatAmount(account.getBalance()));
    }

    public void withdraw() {
        Account account = readExistingAccount();
        if (account == null) {
            return;
        }

        double amount = readPositiveAmount("Enter Withdrawal Amount: ");
        double charges = account.calculateCharges();

        if (account.withdraw(amount)) {
            recordTransaction(account, amount, "WITHDRAW");
            if (charges > 0) {
                System.out.println("Fee Applied : " + formatAmount(charges));
                System.out.println("Total Deducted : " + formatAmount(amount + charges));
            }
            System.out.println("Balance Updated : " + formatAmount(account.getBalance()));
        } else {
            System.out.println("Withdrawal failed: insufficient funds.");
        }
    }

    public void displayAccounts() {
        if (accountCount == 0) {
            System.out.println("No accounts to display.");
            return;
        }
        for (int i = 0; i < accountCount; i++) {
            accounts[i].displayAccount();   // polymorphic dispatch — no casting
        }
    }

    public void displayCustomers() {
        if (customerCount == 0) {
            System.out.println("No customers to display.");
            return;
        }
        for (int i = 0; i < customerCount; i++) {
            customers[i].printDetails();    // or display() — printDetails delegates to it
        }
    }

    public void displayTransactions() {
        if (transactionCount == 0) {
            System.out.println("No transactions to display.");
            return;
        }
        for (int i = 0; i < transactionCount; i++) {
            transactions[i].display();
        }
    }

// ---- Helpers ----

    private void recordTransaction(Account account, double amount, String type) {
        if (transactionCount >= MAX_TRANSACTIONS) {
            System.out.println("Transaction log full — not recorded.");
            return;
        }
        transactions[transactionCount++] = new Transaction(
                "T" + nextTransactionNumber++,
                amount,
                type,
                java.time.LocalDate.now(),
                account.getAccountNumber());
    }

    private Account readExistingAccount() {
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine().trim();
        Account account = findAccount(accountNumber);
        if (account == null) {
            System.out.println("Account not found.");
        }
        return account;
    }

    private Customer findCustomer(String customerId) {
        for (int i = 0; i < customerCount; i++) {
            if (customers[i].getCustomerId().equals(customerId)) {
                return customers[i];
            }
        }
        return null;
    }

    private Account findAccount(String accountNumber) {
        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getAccountNumber().equals(accountNumber)) {
                return accounts[i];
            }
        }
        return null;
    }
}