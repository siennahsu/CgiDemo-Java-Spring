package CgiDemoUnitTests;

import static org.assertj.core.api.Assertions.assertThat;

import CgiDemo.CounterController;
import org.junit.jupiter.api.Test;

import CgiDemo.CounterController;
import CgiDemo.CgiDemoApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = CgiDemoApplication.class)
public class CgiDemoApplicationTest {

    @Autowired
    private CounterController controller;

    @Test
    void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }
}
