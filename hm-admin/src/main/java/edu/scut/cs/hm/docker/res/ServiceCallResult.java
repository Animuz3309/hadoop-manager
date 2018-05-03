package edu.scut.cs.hm.docker.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Result of call service
 */
@Data
public class ServiceCallResult {
    private String message;
    private ResultCode code;
    /**
     * We must not publish this field, because it part of internal api. Used for fine handling of docker errors.
     */
    @JsonIgnore
    private HttpStatus status;

    public ServiceCallResult code(ResultCode code) {
        setCode(code);
        return this;
    }

    public ServiceCallResult message(String message) {
        setMessage(message);
        return this;
    }

    public static ServiceCallResult unsupported() {
        ServiceCallResult scr = new ServiceCallResult();
        scr.setCode(ResultCode.ERROR);
        scr.setMessage("Not supported operation.");
        return scr;
    }

    public static ServiceCallResult unmodified() {
        ServiceCallResult scr = new ServiceCallResult();
        scr.setCode(ResultCode.NOT_MODIFIED);
        scr.setMessage("Not modified.");
        return scr;
    }
}
