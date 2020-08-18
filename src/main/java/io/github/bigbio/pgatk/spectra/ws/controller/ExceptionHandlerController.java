package io.github.bigbio.pgatk.spectra.ws.controller;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

  @Bean
  public ErrorAttributes errorAttributes() {
    // Hide exception field in the return object
    return new DefaultErrorAttributes() {
      @Override
      public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);
        errorAttributes.remove("exception");
        return errorAttributes;
      }
    };
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> constraintViolationException(ConstraintViolationException ex) {

    String s = ex.getConstraintViolations().stream()
            .map(o -> o.getMessage() + ": " + o.getInvalidValue()).collect(Collectors.joining("\n"));

    return new ResponseEntity<>(s, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> illegalArgumentException(IllegalArgumentException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }
}
