package com.academy.bank;

import java.time.LocalDate;

public class Transaction {
    private final String transactionId;
    private final double amount;
    private final String type;
    private final LocalDate date;
    private final String accountNumber;

    public Transaction(String transactionId, double amount, String type,
                       LocalDate date, String accountNumber) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.accountNumber = accountNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void display() {
        System.out.printf(
                "Txn %s | %s | Account: %s | Amount: %.2f | Date: %s%n",
                transactionId, type, accountNumber, amount, date);
    }
}