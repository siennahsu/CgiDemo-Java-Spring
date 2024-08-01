package CgiDemo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CounterNotFoundAdvice {

    @ExceptionHandler(CounterNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String counterNotFoundHandler(CounterNotFoundException ex) {
        return ex.getMessage();
    }
}
