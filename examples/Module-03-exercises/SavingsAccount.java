public class SavingsAccount extends Account {
    public SavingsAccount(double initialBalance) {
        super(initialBalance);  // call Account constructor
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }
}