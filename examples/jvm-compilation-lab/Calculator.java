public class Calculator {
    public static int add(int a, int b) {
        int result = a + b;
        return result;
    }

    public static void main(String[] args) {
        int x = 23;
        int y = 87;
        int sum = add(x, y);

        System.out.println("Sum = " + sum);
    }
}