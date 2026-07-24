import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SortedMapDemo {
    public static void main(String[] args) {
        Map<String, Double> prices = new HashMap<>();

        //   "The Hobbit" -> 12.99, "Dune" -> 15.50, "Annihilation" -> 9.99
        prices.put("The Hobbit", 12.99);
        prices.put("Dune", 15.50);
        prices.put("Annihilation", 9.99);

        System.out.println("HashMap order: " + prices.keySet());

        Map<String, Double> sortedPrices = new TreeMap<>(prices);
        System.out.println("TreeMap order: " + sortedPrices.keySet());

        //   (TreeMap type needed for firstKey / lastKey)
        TreeMap<String, Double> tree = new  TreeMap<>(sortedPrices);
        System.out.println("First title: " + tree.firstKey());
        System.out.println("Last title: " + tree.lastKey());
    }
}