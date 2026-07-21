public class ControlFlow {
    public static void main(String[] args) {
        int number = 4;

        // if: choose one path based on a true/false test
        if (number % 2 == 0) {              // % is remainder; 0 means even
            System.out.println("even");
        } else {
            System.out.println("odd");
        }

        // for: repeat a known number of times (1 through 5)
        for (int i = 1; i <= 5; i++) {
            System.out.println(i);
        }

        // while: repeat while a condition stays true
        int count = 3;
        while (count > 0) {
            System.out.println("countdown " + count);
            count--;                        // decrease so the loop can end
        }

        // switch: pick a label from a fixed set of cases
        int day = 2;
        switch (day) {
            case 1:
                System.out.println("Monday");
                break;                      // leave the switch (don’t fall through)
            case 2:
                System.out.println("Tuesday");
                break;
            default:
                System.out.println("Other day");
                break;
        }
    }
}