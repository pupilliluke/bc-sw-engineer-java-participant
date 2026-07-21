import java.util.Scanner;

public class ProductInfo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Product name: ");
        String name = scanner.nextLine();   // text may include spaces

        System.out.print("Quantity: ");
        // Read a full line, then convert — avoids nextInt leftover-newline issues
        int qty = Integer.parseInt(scanner.nextLine());

        System.out.print("Price: ");
        double price = Double.parseDouble(scanner.nextLine());

        System.out.println("Testing. Enter 1 double, press enter, then 1 int, then 1 string");
        double test1 = scanner.nextDouble(); //not as safe as parseDouble
        int  test2 = scanner.nextInt();         //etc ^

        String next2 = scanner.nextLine();//gets skipped over

        System.out.println("Skipped over: " + next2);

        // %.2f = show two digits after the decimal (money-style)
        System.out.printf("Product: %s | Qty: %d | Price: %.2f%n", name, qty, price);

        scanner.close();
    }
}