package CgiDemo;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class CounterController {

    private final CounterModelAssembler assembler;
    private final CounterServiceImplemented service;

    @Autowired
    CounterController(CounterModelAssembler assembler, CounterServiceImplemented service) {
        this.assembler = assembler;
        this.service = service;

    }


    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("/counters")
    CollectionModel<EntityModel<Counter>> all() {
        List<EntityModel<Counter>> counters = service.findAll().stream()
                .map(assembler::toModel).collect(Collectors.toList());

        return CollectionModel.of(counters,
                linkTo(methodOn(CounterController.class).all()).withSelfRel());
    }
    // end::get-aggregate-root[]


    @PostMapping("/counters")
    Counter newCounter(@RequestBody Counter newCounter) {
        return service.save(newCounter);
    }

    // Single item

    @GetMapping("/counters/{name}")
    EntityModel<Counter> one(@PathVariable String name) {
        Counter counter = service.findById(name);
        return assembler.toModel(counter);
    }

    @PutMapping("/counters/{name}")
    void increaseCounter(@PathVariable String name) {
        service.increaseCounter(name);
    }

    @DeleteMapping("/counters/{name}")
    void deleteCounter(@PathVariable String name) {
        service.deleteCounter(name);
    }

}
