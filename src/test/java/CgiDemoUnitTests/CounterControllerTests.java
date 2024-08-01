package CgiDemoUnitTests;

import CgiDemo.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CounterController.class)
@ContextConfiguration(classes = CgiDemoApplication.class)
public class CounterControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CounterServiceImplemented counterService;

    @MockBean
    private CounterModelAssembler counterModelAssembler;

    // Test for GET /counters
    @Test
    public void testGetAllCounters() throws Exception {
        Counter counter1 = new Counter("counter1", 5);
        Counter counter2 = new Counter("counter2", 10);
        EntityModel<Counter> entityModel1 = EntityModel.of(counter1);
        EntityModel<Counter> entityModel2 = EntityModel.of(counter2);

        when(counterService.findAll()).thenReturn(List.of(counter1, counter2));
        when(counterModelAssembler.toModel(counter1)).thenReturn(entityModel1);
        when(counterModelAssembler.toModel(counter2)).thenReturn(entityModel2);

        mockMvc.perform(get("/counters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.counterList[0].name").value("counter1"))
                .andExpect(jsonPath("$._embedded.counterList[1].name").value("counter2"));
    }

    // Test for POST /counters
    @Test
    public void testCreateNewCounter() throws Exception {
        Counter newCounter = new Counter("counter3", 1);

        when(counterService.save(any(Counter.class))).thenReturn(newCounter);

        mockMvc.perform(post("/counters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"counter3\",\"count\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("counter3"));
    }

    // Test for GET /counters/{name} where the counter DNE
    @Test
    public void testGetCounterByNameNotFound() throws Exception {
        String counterName = "nonexistent";

        doThrow(new CounterNotFoundException(counterName)).when(counterService).findById(counterName);

        mockMvc.perform(get("/counters/" + counterName))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Could not find counter " + counterName));
    }

    // Test for GET /counters/{name}
    @Test
    public void testGetCounterByName() throws Exception {
        Counter counter = new Counter("counter1", 5);
        EntityModel<Counter> entityModel = EntityModel.of(counter);

        when(counterService.findById("counter1")).thenReturn(counter);
        when(counterModelAssembler.toModel(counter)).thenReturn(entityModel);

        mockMvc.perform(get("/counters/counter1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("counter1"))
                .andExpect(jsonPath("$.count").value(5));
    }

    // Test for PUT /counters/{name} where the counter DNE
    @Test
    public void testIncreaseCounterNotFound() throws Exception {
        String counterName = "nonexistent";

        doThrow(new CounterNotFoundException(counterName)).when(counterService).increaseCounter(counterName);

        mockMvc.perform(put("/counters/" + counterName))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Could not find counter " + counterName));
    }

    // Test for PUT /counters/{name}
    @Test
    public void testIncreaseCounter() throws Exception {
        String counterName = "counter1";
        Counter counter = new Counter(counterName, 5); // Initial value is 5
        Counter updatedCounter = new Counter(counterName, 6); // Value after increment should be 6

        when(counterService.findById(counterName)).thenReturn(counter);
        doNothing().when(counterService).increaseCounter(counterName);
        when(counterService.findById(counterName)).thenReturn(updatedCounter);

        mockMvc.perform(put("/counters/" + counterName))
                .andExpect(status().isOk());

        // Verify that the counter was updated
        verify(counterService).increaseCounter(counterName);
        assertEquals(6, counterService.findById(counterName).getCount());
    }

    // Test for DELETE /counters/{name} where counter DNE
    @Test
    public void testDeleteCounterNotFound() throws Exception {
        String counterName = "nonexistent";

        doThrow(new CounterNotFoundException(counterName)).when(counterService).deleteCounter(counterName);

        mockMvc.perform(delete("/counters/" + counterName))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Could not find counter " + counterName));
    }

    // Test for DELETE /counters/{name} where counter value is decreased to 0
    @Test
    public void testDeleteCounterDecreaseToZero() throws Exception {
        String counterName = "counter1";
        Counter counter = new Counter(counterName, 1);

        when(counterService.findById(counterName)).thenReturn(counter);
        doNothing().when(counterService).deleteCounter(counterName);
        // the counter should not exist after being decreased to 0
        doThrow(new CounterNotFoundException(counterName)).when(counterService).findById(counterName);

        mockMvc.perform(delete("/counters/" + counterName))
                .andExpect(status().isOk());

        // Verify that the counter was deleted
        mockMvc.perform(get("/counters/" + counterName))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Could not find counter " + counterName));
    }

    // Test for DELETE /counters/{name}
    @Test
    public void testDeleteCounter() throws Exception {
        String counterName = "counter1";
        Counter counter = new Counter(counterName, 5); // Initial value is 5
        Counter updatedCounter = new Counter(counterName, 4); // Value after decrement should be 4

        when(counterService.findById(counterName)).thenReturn(counter);
        doNothing().when(counterService).deleteCounter(counterName);
        when(counterService.findById(counterName)).thenReturn(updatedCounter);

        mockMvc.perform(delete("/counters/" + counterName))
                .andExpect(status().isOk());

        // Verify that the counter was updated
        verify(counterService).deleteCounter(counterName);
        assertEquals(4, counterService.findById(counterName).getCount());
    }
}
