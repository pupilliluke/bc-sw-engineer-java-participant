import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MapDemo {
    public static void main(String[] args) {
        // Map ISBN-like keys to the number of copies held.
        Map<String, Integer> copies = new HashMap<>();

        // put(key, value) adds a mapping. Keys are unique.
        copies.put("ISBN-JAVA", 3);
        copies.put("ISBN-CLEAN", 2);
        copies.put("ISBN-TEST", 4);

        System.out.println(
                "Java copies: " + copies.get("ISBN-JAVA"));

        // put on an existing key replaces the value — it does not
        // create a second "ISBN-JAVA" entry. 3 becomes 5.
        copies.put("ISBN-JAVA", 5);

        // Remove the whole mapping for this key.
        copies.remove("ISBN-CLEAN");

        System.out.println(
                "Updated Java copies: "
                        + copies.get("ISBN-JAVA"));

        // get on an absent key returns null; getOrDefault supplies a
        // fallback instead, which avoids an unboxing NullPointerException.
        System.out.println(
                "Missing ISBN: "
                        + copies.getOrDefault("ISBN-MISSING", 0));

        // entrySet gives key-value pairs. HashMap order is unspecified.
        for (Map.Entry<String, Integer> entry
                : copies.entrySet()) {
            System.out.println(
                    entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println(copies.get("ISBN-MISSING")); //null


        // TreeMap copy sorts by key, so this line is deterministic.
        System.out.println(
                "Sorted snapshot: " + new TreeMap<>(copies));
    }
}
