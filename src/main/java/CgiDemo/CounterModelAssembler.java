package CgiDemo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class CounterModelAssembler implements RepresentationModelAssembler<Counter, EntityModel<Counter>> {

    @Override
    public EntityModel<Counter> toModel(Counter counter) {

        return EntityModel.of(counter,
                linkTo(methodOn(CounterController.class).one(counter.getName())).withSelfRel(),
                linkTo(methodOn(CounterController.class).all()).withRel("counters"));
    }

}
