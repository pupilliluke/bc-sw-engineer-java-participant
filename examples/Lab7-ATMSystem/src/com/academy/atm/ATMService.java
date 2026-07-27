package com.academy.atm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ATMService {

    private static final int MAX_PIN_ATTEMPTS = 3;
    private static final int ERROR_PREVIEW_LIMIT = 5;
    private static final String TRANSACTION_FILE = "transactions.txt";

    private final Map<String, Account> accounts = new HashMap<>();
    private final List<Transaction> sessionTransactions = new ArrayList<>();
    private final Scanner scanner;

    private Account loggedInAccount;
    private int pinAttemptsRemaining = MAX_PIN_ATTEMPTS;

    public ATMService(Scanner scanner) {
        this.scanner = scanner;
        initializeAccounts();
    }

    private void initializeAccounts() {
        accounts.put("1001", new Account("1001", "John Smith", "1234", 11000));
        accounts.put("1002", new Account("1002", "Alice Johnson", "5678", 5000));
    }

    private Account findAccount(String accountNumber) throws AccountNotFoundException {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        return account;
    }

    private void printReturnMessage() {
        System.out.println("Transaction Completed.");
        System.out.println("Returning to Main Menu.");
    }

    public void login() {
        if (loggedInAccount != null) {
            System.out.println("Already logged in as " + loggedInAccount.getCustomerName() + ".");
            return;
        }

        if (pinAttemptsRemaining <= 0) {
            System.out.println("ERROR");
            System.out.println("Login locked for this session. Restart the ATM to try again.");
            LoggerUtil.logError("Login attempted while locked", new InvalidPinException(
                    "Login locked for this session.", pinAttemptsRemaining));
            printReturnMessage();
            return;
        }

        System.out.print("Enter Account Number : ");
        String accountNumber = scanner.nextLine().trim();

        try {
            Account account = findAccount(accountNumber);
            System.out.print("Enter PIN : ");
            String pin = scanner.nextLine().trim();

            if (!account.getPin().equals(pin)) {
                pinAttemptsRemaining--;
                throw new InvalidPinException("Invalid PIN entered.", pinAttemptsRemaining);
            }

            loggedInAccount = account;
            pinAttemptsRemaining = MAX_PIN_ATTEMPTS;
            System.out.println("Login Successful");
            LoggerUtil.logInfo("Login successful for account " + accountNumber);
        } catch (AccountNotFoundException | InvalidPinException ex) {
            System.out.println("ERROR");
            System.out.println(ex.getMessage());
            LoggerUtil.logError(ex.getMessage(), ex);

            if (ex instanceof InvalidPinException invalidPin) {
                if (invalidPin.getAttemptsRemaining() <= 0) {
                    System.out.println("Maximum PIN attempts reached. Login locked for this session.");
                } else {
                    System.out.println("Attempts remaining : " + invalidPin.getAttemptsRemaining());
                }
            }
        } finally {
            printReturnMessage();
        }
    }

    public void deposit() {
        executeTransaction("Deposit", () -> {
            requireLogin();
            double amount = readAmount("Amount : ");
            loggedInAccount.deposit(amount);
            recordTransaction("DEPOSIT", amount, true, "Deposit successful");
            System.out.println("Deposit Successful");
            System.out.printf("Current Balance : %.0f%n", loggedInAccount.getBalance());
        });
    }

    public void withdraw() {
        executeTransaction("Withdraw", () -> {
            requireLogin();
            double amount = readAmount("Amount : ");
            loggedInAccount.withdraw(amount);
            recordTransaction("WITHDRAW", amount, true, "Withdrawal successful");
            System.out.println("Withdrawal Successful");
            System.out.printf("Current Balance : %.0f%n", loggedInAccount.getBalance());
        });
    }

    public void displayBalance() {
        executeTransaction("Balance Inquiry", () -> {
            requireLogin();
            loggedInAccount.displayBalance();
        });
    }

    public void transferFunds() {
        executeTransaction("Transfer", () -> {
            requireLogin();
            System.out.print("Destination Account Number : ");
            String destinationAccountNumber = scanner.nextLine().trim();
            Account destinationAccount = findAccount(destinationAccountNumber);
            double amount = readAmount("Transfer Amount : ");

            String sourceAccountNumber = loggedInAccount.getAccountNumber();
            double sourceBalanceBefore = loggedInAccount.getBalance();
            double destinationBalanceBefore = destinationAccount.getBalance();

            try {
                loggedInAccount.withdraw(amount);
                destinationAccount.deposit(amount);
                recordTransaction("TRANSFER_OUT", amount, true,
                        "Transfer to " + destinationAccountNumber);
                recordTransaction("TRANSFER_IN", amount, true,
                        "Transfer from " + sourceAccountNumber);
                System.out.println("Transfer Successful");
            } catch (Exception ex) {
                accounts.get(sourceAccountNumber).restoreBalance(sourceBalanceBefore);
                accounts.get(destinationAccountNumber).restoreBalance(destinationBalanceBefore);
                System.out.println("Transfer rolled back due to transaction failure.");
                LoggerUtil.logInfo("Transfer rollback completed for accounts "
                        + sourceAccountNumber + " and " + destinationAccountNumber);
                throw ex;
            }
        });
    }

    public void displayMiniStatement() {
        executeTransaction("Mini Statement", () -> {
            requireLogin();
            System.out.println("Session Transactions:");
            sessionTransactions.stream()
                    .filter(transaction -> loggedInAccount.getAccountNumber().equals(transaction.getAccountNumber()))
                    .forEach(System.out::println);

            System.out.println();
            System.out.println("Historical Transactions (from file):");
            loadTransactionsFromFile();
        });
    }

    public void demonstrateUncheckedExceptions() {
        System.out.println("--- Unchecked Exception Demonstrations ---");

        try {
            Account account = null;
            account.getBalance();
        } catch (NullPointerException ex) {
            System.out.println("Handled NullPointerException gracefully.");
            LoggerUtil.logError("NullPointerException demonstration", ex);
        }

        try {
            int result = 10 / 0;
            System.out.println(result);
        } catch (ArithmeticException ex) {
            System.out.println("Handled ArithmeticException gracefully.");
            LoggerUtil.logError("ArithmeticException demonstration", ex);
        }

        try {
            int[] values = {1, 2, 3};
            System.out.println(values[10]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Handled ArrayIndexOutOfBoundsException gracefully.");
            LoggerUtil.logError("ArrayIndexOutOfBoundsException demonstration", ex);
        }
    }

    public void generateDailyErrorReport() {
        System.out.println("--- Daily Error Report ---");

        String today = LocalDate.now().toString();
        Map<String, Integer> errorsByDate = new TreeMap<>();
        List<String> todaysErrors = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(LoggerUtil.getLogPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!isErrorEntry(line)) {
                    continue;
                }
                String date = line.substring(0, 10);
                errorsByDate.merge(date, 1, Integer::sum);
                if (date.equals(today)) {
                    todaysErrors.add(line);
                }
            }
        } catch (IOException ex) {
            System.out.println("Unable to read the log file.");
            LoggerUtil.logError("Unable to read log file for daily error report", ex);
            return;
        }

        if (errorsByDate.isEmpty()) {
            System.out.println("No errors logged.");
            return;
        }

        errorsByDate.forEach((date, count) -> System.out.printf("%s : %d%n", date, count));

        int total = errorsByDate.values().stream().mapToInt(Integer::intValue).sum();
        System.out.printf("Total errors : %d%n", total);
        System.out.printf("Today (%s) : %d%n", today, errorsByDate.getOrDefault(today, 0));

        if (todaysErrors.isEmpty()) {
            return;
        }

        int previewFrom = Math.max(0, todaysErrors.size() - ERROR_PREVIEW_LIMIT);
        System.out.printf("Most recent %d of today:%n", todaysErrors.size() - previewFrom);
        todaysErrors.subList(previewFrom, todaysErrors.size())
                .forEach(entry -> System.out.println("  " + entry));
    }

    private boolean isErrorEntry(String line) {
        return line.length() > 25 && line.startsWith("ERROR", 20);
    }

    public void generateTransactionSummary() {
        System.out.println("--- Transaction Summary ---");

        if (sessionTransactions.isEmpty()) {
            System.out.println("No transactions recorded this session.");
            return;
        }

        long successful = sessionTransactions.stream()
                .filter(Transaction::isSuccessful)
                .count();

        System.out.printf("Total      : %d%n", sessionTransactions.size());
        System.out.printf("Successful : %d%n", successful);
        System.out.printf("Failed     : %d%n", sessionTransactions.size() - successful);

        Map<String, Long> byType = sessionTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getType, TreeMap::new, Collectors.counting()));

        System.out.println("By type:");
        byType.forEach((type, count) -> System.out.printf("  %-13s %d%n", type, count));
    }

    public void logout() {
        loggedInAccount = null;
    }

    private void executeTransaction(String operationName, TransactionAction action) {
        long startTime = System.nanoTime();
        try {
            action.run();
            LoggerUtil.logTransaction(operationName + " completed successfully",
                    (System.nanoTime() - startTime) / 1_000_000);
        } catch (InputMismatchException ex) {
            System.out.println("ERROR");
            System.out.println("Invalid numeric input.");
            System.out.println("Please enter a valid amount.");
            LoggerUtil.logError("Invalid numeric input during " + operationName, ex);
            recordFailedTransaction(operationName, ex.getMessage());
        } catch (InvalidAmountException ex) {
            System.out.println("ERROR");
            System.out.println(ex.getMessage());
            LoggerUtil.logError(ex.getMessage(), ex);
            recordFailedTransaction(operationName, ex.getMessage());
        } catch (InsufficientFundsException ex) {
            System.out.println("ERROR");
            System.out.println("Insufficient Balance");
            System.out.println("Transaction Cancelled");
            LoggerUtil.logError("Requested " + ex.getRequestedAmount()
                    + " Balance " + ex.getAvailableBalance(), ex);
            recordFailedTransaction(operationName, ex.getMessage());
        } catch (InvalidPinException | AccountNotFoundException ex) {
            System.out.println("ERROR");
            System.out.println(ex.getMessage());
            LoggerUtil.logError(ex.getMessage(), ex);
            recordFailedTransaction(operationName, ex.getMessage());
        } catch (Exception ex) {
            System.out.println("ERROR");
            System.out.println("Unexpected error occurred. Please try again.");
            LoggerUtil.logError("Unexpected error during " + operationName, ex);
            recordFailedTransaction(operationName, ex.getMessage());
        } finally {
            printReturnMessage();
        }
    }

    private void loadTransactionsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException ex) {
            System.out.println("Unable to read transaction history.");
            LoggerUtil.logError("Unable to read transaction history", ex);
        }
    }

    private void requireLogin() throws InvalidPinException {
        if (loggedInAccount == null) {
            throw new InvalidPinException("Please login before performing this operation.", pinAttemptsRemaining);
        }
    }

    private double readAmount(String prompt) throws InputMismatchException {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException ex) {
            throw new InputMismatchException("Invalid numeric input.");
        }
    }

    private void recordTransaction(String type, double amount, boolean successful, String details) {
        if (loggedInAccount == null) {
            return;
        }
        sessionTransactions.add(new Transaction(
                loggedInAccount.getAccountNumber(), type, amount, successful, details));
    }

    private void recordFailedTransaction(String type, String details) {
        if (loggedInAccount == null) {
            return;
        }
        sessionTransactions.add(new Transaction(
                loggedInAccount.getAccountNumber(), type.toUpperCase(), 0, false, details));
    }

    @FunctionalInterface
    private interface TransactionAction {
        void run() throws Exception;
    }
}
