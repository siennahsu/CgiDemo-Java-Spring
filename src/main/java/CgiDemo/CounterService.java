package CgiDemo;

import java.util.List;

public interface CounterService {
    List<Counter> findAll();
    Counter save(Counter newCounter);
    Counter findById(String name);
    void increaseCounter(String name);
    void deleteCounter(String name);
}
