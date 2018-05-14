package edu.scut.cs.hm.admin.web.handler;

import edu.scut.cs.hm.admin.web.model.error.UiError;
import edu.scut.cs.hm.common.security.token.TokenException;
import edu.scut.cs.hm.common.utils.Throwables;
import edu.scut.cs.hm.model.HttpException;
import edu.scut.cs.hm.model.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import java.net.BindException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Error handler: format error messages
 */
@ControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(BindException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public UiError bindErrorHandler(org.springframework.validation.BindException bindException) {
        log.error("Can't process request", bindException);
        return createResponse(bindException.getMessage(), bindException.getAllErrors().toString(), BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public UiError bindErrorHandler(MethodArgumentNotValidException bindException) {
        log.error("Can't process request", bindException);
        return createResponse(bindException.getMessage(), bindException.getBindingResult().getAllErrors().toString(), BAD_REQUEST);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public UiError bindErrorHandler(IllegalArgumentException e) {
        log.error("Can't process request", e);
        return createResponse(e.getMessage(), Throwables.printToString(e), BAD_REQUEST);
    }

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public UiError bindErrorHandler(NotFoundException e) {
        log.error("Can't process request", e);
        return createResponse(e.getMessage(), Throwables.printToString(e), NOT_FOUND);
    }

    @ExceptionHandler({HttpClientErrorException.class})
    @ResponseBody
    public ResponseEntity<UiError> bindErrorHandler(HttpClientErrorException e) {
        log.error("Can't process request", e);
        HttpStatus statusCode = e.getStatusCode();
        UiError response = createResponse(StringUtils.trimWhitespace(e.getResponseBodyAsString()),
                Throwables.printToString(e), statusCode);
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<UiError> handleHttpExcepion(HttpException ex) {
        log.error("Can't process request", ex);
        UiError error = UiError.from(ex);
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler()
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public UiError internalError(HttpServletRequest req, Exception e) {
        log.error("Can't process request", e);
        return createResponse(e.getMessage(), Throwables.printToString(e), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({TokenException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public UiError badCredentialsException(Exception e) {
        log.error("Can't process request", e);
        return createResponse(e.getMessage(), e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    static UiError createResponse(String message, String stackTrace, HttpStatus httpStatus) {
        UiError uiError = new UiError();
        uiError.setStack(stackTrace);
        uiError.setMessage(message);
        uiError.setCode(httpStatus.value());
        return uiError;
    }

}
