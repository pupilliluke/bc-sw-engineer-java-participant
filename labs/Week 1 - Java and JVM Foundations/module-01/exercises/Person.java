public class Person {
    // Fields (instance data) — each Person object has its own copy
    String name;
    int age;

    // Constructor — runs when you write new Person(...)
    public Person(String name, int age) {
        this.name = name;                   // this.name = field; name = parameter
        this.age = age;
    }

    // Instance method — uses this object’s fields
    public void display() {
        System.out.println(name + " is " + age + " years old");
    }

    public static void main(String[] args) {
        // Create one Person on the heap; person holds a reference (on the stack)
        Person person = new Person("Aman", 21);
        person.display();                   // call method on that object
    }
}