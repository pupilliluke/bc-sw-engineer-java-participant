import java.util.ArrayList;
import java.util.List;

public class ArrayListDemo {
    public static void main(String[] args) {
        // Declare an ordered list of book titles.
        List<String> books = new ArrayList<>();

        // Add four titles — "Java Fundamentals" appears twice (duplicates allowed).
        books.add("Java Fundamentals");
        books.add("Clean Code");
        books.add("Effective Java");
        books.add("Java Fundamentals");

        // Replace the value at index 1 (does not change the size).
        books.set(1, "Clean Architecture");

        // Search — contains(...) uses equals.
        boolean found = books.contains("Effective Java");

        // Remove the first "Java Fundamentals" only.
        // remove(Object) deletes the first match; the second copy stays.
        books.remove("Java Fundamentals");

        System.out.println(
                "Found Effective Java: " + found);
        System.out.println("Size: " + books.size());

        // Print index and title for each element.
        for (int i = 0; i < books.size(); i++) {
            System.out.printf(
                    "%d: %s%n", i, books.get(i));
        }
//        System.out.println(books.get(99));

    }
}
