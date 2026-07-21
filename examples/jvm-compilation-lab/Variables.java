// Class name must match file name Variables.java
public class Variables {
    // Entry point — JVM starts here
    public static void main(String[] args) {
        int age = 21;                       // whole number (32-bit)
        long population = 8_000_000_000L;   // bigger whole number; L means long
        double price = 19.99;               // decimal number
        boolean enrolled = true;            // true or false only
        char grade = 'A';                  // single character in single quotes
        String name = "Aman";               // text (object) in double quotes

        System.out.println(age);            // print each value on its own line
        System.out.println(population);
        System.out.println(price);
        System.out.println(enrolled);
        System.out.println(grade);
        System.out.println(name);
    }
}