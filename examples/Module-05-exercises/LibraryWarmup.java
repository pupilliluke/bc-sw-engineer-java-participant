import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryWarmup {
    private final List<String> availableTitles =
            new ArrayList<String>();

    private final Map<String, String> borrowedByMember =
            new HashMap<String, String>();

    public LibraryWarmup() {
        availableTitles.add("Effective Java");
        availableTitles.add("Clean Code");
    }

    boolean checkout(String memberId, String title) {
        if (borrowedByMember.containsKey(memberId)) {
            return false;
        }

        if (!availableTitles.remove(title)) {
            return false;
        }

        borrowedByMember.put(memberId, title);
        return true;
    }

    void printStatus() {
        System.out.println(
                "Available: " + availableTitles);
        System.out.println(
                "Borrowed: " + borrowedByMember);
    }

    public static void main(String[] args) {
        LibraryWarmup library = new LibraryWarmup();

        System.out.println(
                "Checkout success: "
                        + library.checkout(
                        "M101", "Effective Java"));

        System.out.println(
                "Duplicate checkout: "
                        + library.checkout(
                        "M101", "Clean Code"));

        System.out.println(
                "Missing title: "
                        + library.checkout("M102", "Unknown Book"));
        library.printStatus();
    }
}