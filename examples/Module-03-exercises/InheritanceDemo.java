public class InheritanceDemo {
    public static void main(String[] args) {
        // Base-type array can hold either subclass.
        Account[] accounts = {
                new SavingsAccount(100.00),
                new CurrentAccount(100.00)
        };

        for (Account account : accounts) {
            // Runtime type chooses the overridden method.
            account.withdraw(20.00);
            System.out.printf("%s balance: %.2f%n",
                    account.getAccountType(),
                    account.getBalance());
        }
    }
}