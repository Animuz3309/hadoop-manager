package edu.scut.cs.hm.admin.web.model;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * DTO used for result
 */
@Data
public class UiResult {

    /**
     * Code like http code
     */
    private int code;

    /**
     * User friendly error message
     */
    private String message;

    /**
     * code like HTTP CODE
     * @param value
     * @return
     */
    public UiResult code(int value) {
        setCode(value);
        return this;
    }

    /**
     * code like HTTP CODE
     * @param value
     * @return
     */
    public UiResult code(HttpStatus value) {
        return code(value.value());
    }

    /**
     * User friendly error message
     */
    public String getMessage() {
        if (message != null) {
            return message;
        } else {
            return "";
        }
    }

}
