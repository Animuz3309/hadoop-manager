package edu.scut.cs.hm.admin.web.model.error;

import edu.scut.cs.hm.admin.web.model.UiResult;
import edu.scut.cs.hm.common.utils.Throwables;
import edu.scut.cs.hm.model.HttpException;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO used for ui error
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UiError extends UiResult {

    /**
     * Stack trace of error
     */
    private String stack;

    public static UiError from(Exception ex) {
        UiError error = new UiError();
        error.setStack(Throwables.printToString(ex));
        if (ex instanceof HttpException) {
            error.setCode(((HttpException)ex).getStatus().value());
        }
        error.setMessage(ex.getMessage());
        return error;
    }
}
