package com.academy.bank;

public abstract class Account {
    private String accountNumber;
    private double balance;
    private Customer customer;

    protected Account(String accountNumber, double balance, Customer customer) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.customer = customer;
    }

    // Getters
    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public Customer getCustomer() {
        return customer;
    }

    // Protected so only subclasses can adjust balance directly
    protected void setBalance(double balance) {
        this.balance = balance;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Deposit rejected: amount must be positive.");
            return;
        }
        setBalance(balance + amount);
    }

    public boolean withdraw(double amount) {
        if (amount <= 0) {
            return false;
        }
        double total = amount + calculateCharges();
        if (total > balance) {
            return false;
        }
        setBalance(balance - total);
        return true;
    }

    // Subclasses must implement
    public abstract void displayAccount();

    // Defaults — subclasses override as needed
    public double calculateCharges() {
        return 0.0;
    }

    public double calculateInterest() {
        return 0.0;
    }

    public String getAccountType() {
        return "Account";
    }
}