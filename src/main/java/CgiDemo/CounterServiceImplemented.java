package CgiDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CounterServiceImplemented implements CounterService {

    private final CounterRepository repository;

    @Autowired
    public CounterServiceImplemented(CounterRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Counter> findAll() {
        return repository.findAll();
    }

    @Override
    public Counter save(Counter newCounter) {
        return repository.save(newCounter);
    }

    @Override
    public Counter findById(String name) {
        return repository.findById(name).orElseThrow(() -> new CounterNotFoundException(name));
    }

    @Override
    public void increaseCounter(String name) {
        Counter counter = repository.findById(name).orElseThrow(() -> new CounterNotFoundException(name));
        counter.setCount(counter.getCount() + 1);
        repository.save(counter);
    }

    @Override
    public void deleteCounter(String name) {
        Counter counter = repository.findById(name).orElseThrow(() -> new CounterNotFoundException(name));
        counter.setCount(counter.getCount() - 1);

        if (counter.getCount() <= 0) {
            repository.deleteById(name);
        } else {
            repository.save(counter);
        }
    }
}
