import java.util.Scanner;

public class PersonalProfile {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Age: ");
        int age = Integer.parseInt(scanner.nextLine());    // changed to parseInt

        System.out.print("City: ");
        String city = scanner.nextLine();

        System.out.print("Hobby: ");
        String hobby = scanner.nextLine();

        System.out.println();
        // %-12s = left-align in a 12-character field; %-20s = 20-character field
        System.out.printf("%-12s | %-20s%n", "Field", "Value");
        System.out.println("-------------|---------------");
        System.out.printf("%-12s | %-20s%n", "Name", name);
        System.out.printf("%-12s | %-20d%n", "Age", age); //changed to d% for int
        System.out.printf("%-12s | %-20s%n", "City", city);
        System.out.printf("%-12s | %-20s%n", "Hobby", hobby);
        System.out.printf("%-12s | %-20s%n", "Finished?", "Yes"); //bonus row!

        scanner.close();
    }
}