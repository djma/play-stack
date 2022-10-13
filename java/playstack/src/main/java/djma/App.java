package djma;

/**
 * Hello world!
 *
 */
public class App {
    public record Person(String name, int age) {
    };

    public static void main(String[] args) {
        var p = new Person("John", 42);
        System.out.println("Hello %s!".formatted(p.name()));
    }
}
