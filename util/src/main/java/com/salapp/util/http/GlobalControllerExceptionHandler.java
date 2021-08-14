package com.salapp.util.http;

import com.salapp.util.exceptions.InvalidInputException;
import com.salapp.util.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    HttpErrorInfo handleNotFoundException(ServerHttpRequest request, Exception e) {
        return createHttpErrorInfo(NOT_FOUND, request, e);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public @ResponseBody
    HttpErrorInfo handleInvalidInputException(ServerHttpRequest request, Exception e) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, e);
    }

    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest request, Exception e) {
        final String path = request.getPath().pathWithinApplication().value();
        final String message = e.getMessage();

        LOG.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);
        return new HttpErrorInfo(httpStatus, path, message);
    }
}
