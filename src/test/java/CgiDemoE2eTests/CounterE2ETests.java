package CgiDemoE2eTests;

import CgiDemo.CgiDemoApplication;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import CgiDemo.Counter;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = CgiDemoApplication.class)
public class CounterE2ETests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/counters";
    }

    // Test for POST
    @Test
    public void testCreateCounter() {
        // POST a new counter
        Counter newCounter = new Counter("counter1", 5);
        ResponseEntity<Counter> response = restTemplate.postForEntity(baseUrl, newCounter, Counter.class);

        // Assert status code
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        // Verify the counter is retrievable via GET
        ResponseEntity<Counter> getResponse = restTemplate.getForEntity(baseUrl + "/counter1", Counter.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Counter retrievedCounter = getResponse.getBody();
        assertThat(retrievedCounter).isNotNull();
        assertThat(retrievedCounter.getName()).isEqualTo("counter1");
        assertThat(retrievedCounter.getCount()).isEqualTo(5);
    }

    // Test for Get all counters
    @Test
    public void testGetAllCounters() {
        // Post some counters
        Counter counter1 = new Counter("counter1", 5);
        Counter counter2 = new Counter("counter2", 10);
        restTemplate.postForEntity(baseUrl, counter1, Counter.class);
        restTemplate.postForEntity(baseUrl, counter2, Counter.class);

        // GET all counters
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);

        // Assert the status code
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        // Parse the response body to JSON
        String responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        DocumentContext jsonContext = JsonPath.parse(responseBody);

        // Verify the structure and contents of the JSON response
        List<String> counterNames = jsonContext.read("$._embedded.counterList[*].name");
        List<Integer> counterCounts = jsonContext.read("$._embedded.counterList[*].count");
        assertThat(counterNames).hasSize(2);
        assertThat(counterCounts).hasSize(2);
        assertThat(counterNames).containsExactlyInAnyOrder("counter1", "counter2");
        assertThat(counterCounts).containsExactlyInAnyOrder(5, 10);
    }

    // Test for Get by ID (name)
    @Test
    public void testGetCounterByName() {
        // Post a counter for testing
        Counter counter = new Counter("counter1", 5);
        restTemplate.postForEntity(baseUrl, counter, Counter.class);

        // GET the counter by name
        ResponseEntity<Counter> response = restTemplate.getForEntity(baseUrl + "/counter1", Counter.class);

        // Assert status code
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        // Assert response body
        Counter retrievedCounter = response.getBody();
        assertThat(retrievedCounter).isNotNull();
        assertThat(retrievedCounter.getName()).isEqualTo("counter1");
        assertThat(retrievedCounter.getCount()).isEqualTo(5);
    }

    @Test
    public void testGetCounterByNameNotFound() {
        // GET a counter that DNE
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/nonexistent", String.class);

        // Assert status code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    // Test for PUT
    @Test
    public void testIncreaseCounter() {
        // Post a counter
        Counter counter = new Counter("counter1", 5);
        restTemplate.postForEntity(baseUrl, counter, Counter.class);

        // Increase the counter via PUT
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange(baseUrl + "/counter1", HttpMethod.PUT, requestEntity, Void.class);

        // Assert status code
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        // Verify that the counter was increased via GET
        ResponseEntity<Counter> updatedCounterResponse = restTemplate.getForEntity(baseUrl + "/counter1", Counter.class);
        Counter updatedCounter = updatedCounterResponse.getBody();
        assertThat(updatedCounter).isNotNull();
        assertThat(updatedCounter.getCount()).isEqualTo(6);
    }

    @Test
    public void testIncreaseCounterNotFound() {
        // Create a counter but do not POST it
        Counter updatedCounter = new Counter("nonexistent", 10);
        HttpEntity<Counter> request = new HttpEntity<>(updatedCounter);

        // PUT the non-existent counter
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/nonexistent", HttpMethod.PUT, request, String.class);

        // Assert status code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    // Test for DELETE
    @Test
    public void testDeleteCounter() {
        // Post a counter whose value will be decreased but not deleted
        Counter counter = new Counter("counter1", 5);
        restTemplate.postForEntity(baseUrl, counter, Counter.class);

        // DELETE (Decrease) the counter
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange(baseUrl + "/counter1", HttpMethod.DELETE, requestEntity, Void.class);

        // Assert status code
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        // Verify that the counter's value was decreased by 1 via GET
        ResponseEntity<Counter> updatedCounterResponse = restTemplate.getForEntity(baseUrl + "/counter1", Counter.class);
        Counter updatedCounter = updatedCounterResponse.getBody();
        assertThat(updatedCounter).isNotNull();
        assertThat(updatedCounter.getCount()).isEqualTo(4);
    }

    @Test
    public void testDeleteCounterToZero() {
        // Post a counter that will be deleted after calling DELETE
        Counter counter = new Counter("counter1", 1);
        restTemplate.postForEntity(baseUrl, counter, Counter.class);

        // DELETE the counter
        restTemplate.delete(baseUrl + "/counter1"); // since the count drops to 0, it should be deleted

        // Verify that the counter DNE via GET
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/counter1", String.class);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    public void testDeleteCounterNotFound() {
        // Create a counter but do not POST it
        Counter updatedCounter = new Counter("nonexistent", 10);
        HttpEntity<Counter> request = new HttpEntity<>(updatedCounter);

        // DELETE the non-existent counter
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/nonexistent", HttpMethod.DELETE, request, String.class);

        // Assert status code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
