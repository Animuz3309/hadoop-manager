package edu.scut.cs.hm.admin.web.controller;

import edu.scut.cs.hm.common.security.token.TokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Slf4j
@Controller("${server.error.path:${error.path:/error}}")
public final class CustomerErrorController extends AbstractErrorController {
    private static final String ERROR_PATH_PREFIX = "error/";
    private final ErrorProperties errorProperties;

    @Autowired
    public CustomerErrorController(ErrorAttributes errorAttributes, ServerProperties serverProperties) {
        super(errorAttributes);
        Assert.notNull(serverProperties, "ServerProperties must not be null");
        Assert.notNull(serverProperties.getError(), "ErrorProperties must not be null");
        this.errorProperties = serverProperties.getError();
    }

    @Override
    public String getErrorPath() {
        return this.errorProperties.getPath();
    }

    @RequestMapping(produces = TEXT_HTML_VALUE)
    @ExceptionHandler(Exception.class)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView;

        HttpStatus status = getStatus(request);
        response.setStatus(status.value());

        switch (status) {
            case BAD_REQUEST:
                modelAndView = new ModelAndView(ERROR_PATH_PREFIX + "400");
                break;
            case UNAUTHORIZED:
                modelAndView = new ModelAndView(ERROR_PATH_PREFIX + "401");
                break;
            case FORBIDDEN:
                modelAndView = new ModelAndView(ERROR_PATH_PREFIX + "403");
                break;
            case NOT_FOUND:
                modelAndView = new ModelAndView(ERROR_PATH_PREFIX + "404");
                break;
            case INTERNAL_SERVER_ERROR:
                modelAndView = new ModelAndView(ERROR_PATH_PREFIX + "500");
                break;
            default:
                modelAndView = new ModelAndView("error");
        }
        Map<String, Object> model = Collections.unmodifiableMap(
                getErrorAttributes(request, isIncludeStackTrace(request)));
        model.forEach(modelAndView::addObject);
        return modelAndView;
    }

    /**
     * Handle restful request
     * @param request
     * @param ex
     * @return
     */
    @RequestMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request, Throwable ex) {
        log.error("Caught a {}: {}", ex.getClass().getName(), ex.getMessage(), ex);
        Map<String, Object> body = getErrorAttributes(request, isIncludeStackTrace(request));
        HttpStatus status = getStatus(request);
        return new ResponseEntity<>(body, status);
    }

    private boolean isIncludeStackTrace(HttpServletRequest request) {
        ErrorProperties.IncludeStacktrace include = getErrorProperties().getIncludeStacktrace();
        if (include == ErrorProperties.IncludeStacktrace.ALWAYS) {
            return true;
        }
        if (include == ErrorProperties.IncludeStacktrace.ON_TRACE_PARAM) {
            return getTraceParameter(request);
        }
        return false;
    }

    private ErrorProperties getErrorProperties() {
        return this.errorProperties;
    }
}
