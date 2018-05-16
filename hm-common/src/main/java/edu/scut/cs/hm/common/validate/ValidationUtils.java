package edu.scut.cs.hm.common.validate;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

public class ValidationUtils {
    /**
     * Throws exception when specified object is invalid.
     * @param validator validator
     * @param obj validating object
     * @param id string which can identify object
     * @param msg message whic pass to exception, mai be null
     * @param <T> type of object
     * @throws ValidityException
     */
    public static <T> void assertValid(Validator validator, T obj, String id, String msg) throws ValidityException {
        Set<ConstraintViolation<T>> res = validator.validate(obj);
        Validity validity = new JsrValidityImpl(id, res);
        if(!validity.isValid()) {
            if(msg == null) {
                msg = "Invalid: " + id;
            }
            throw new ValidityException(msg, validity);
        }
    }
}
