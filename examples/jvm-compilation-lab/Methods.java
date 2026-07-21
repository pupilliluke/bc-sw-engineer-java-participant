public class Methods {
    // Entry point — call other methods from here
    public static void main(String[] args) {
        int sum = add(10, 20);              // call add; store returned int
        System.out.println(sum);            // expect 30

        String message = greet("Aman");     // call greet; store returned String
        System.out.println(message);        // expect Hello, Aman!
    }

    // Takes two ints (parameters a, b); returns their sum
    public static int add(int a, int b) {
        return a + b;                       // send result back to the caller
    }

    // Takes a String name; returns a greeting String
    public static String greet(String name) {
        return "Hello, " + name + "!";      // + joins text pieces
    }
}