public class FrozenAccount extends Account {
    public FrozenAccount(double initialBalance) {
        // Reuse Account construction and balance encapsulation.
        super(initialBalance);
    }

    @Override
    public boolean withdraw(double amount) {
        // Frozen accounts refuse every withdrawal but still honor the boolean contract.
        return false;
    }

    @Override
    public String getAccountType() {
        return "Frozen";
    }
}