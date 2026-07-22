public class Account {
    // Hidden state: outside code cannot write account.balance directly
    private double balance;

    public Account(double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException(
                    "Initial balance cannot be negative");
        }
        balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println(
                    "Deposit rejected: amount must be positive.");
            return;
        }
        balance += amount;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            System.out.println("Withdrawal rejected.");
            return false;
        }
        balance -= amount;
        return true;
    }

    // Read-only access: callers can inspect, but not assign, the balance
    public double getBalance() {
        return balance;
    }

    // Exercise 3 will override this method
    public String getAccountType() {
        return "Account";
    }
}