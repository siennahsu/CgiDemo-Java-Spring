package CgiDemo;

public class CounterNotFoundException extends RuntimeException{

    public CounterNotFoundException(String name) {
        super("Could not find counter " + name);
    }
}
