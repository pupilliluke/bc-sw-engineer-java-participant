public class InterfaceDemo {
    public static void main(String[] args) {
        // Variable knows only the Printable contract.
        Printable printable =
                new Customer("C101", "Aman Singh");

        printable.printDetails();
    }
}