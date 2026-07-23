package com.academy.bank;

public class SavingsAccount extends Account implements Printable {
    private double interestRate;

    public SavingsAccount(String accountNumber, double balance,
                          Customer customer, double interestRate) {
        super(accountNumber, balance, customer);
        this.interestRate = interestRate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    @Override
    public double calculateInterest() {
        return getBalance() * interestRate / 100.0;
    }

    @Override
    public void displayAccount() {
        System.out.printf(
                "%s Account %s | Customer: %s | Balance: %.2f | Rate: %.2f%% | Interest: %.2f%n",
                getAccountType(),
                getAccountNumber(),
                getCustomer().getName(),
                getBalance(),
                interestRate,
                calculateInterest());
    }

    @Override
    public void printDetails() {
        displayAccount();
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }
}