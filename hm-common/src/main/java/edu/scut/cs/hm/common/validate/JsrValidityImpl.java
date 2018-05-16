package edu.scut.cs.hm.common.validate;

import org.springframework.util.Assert;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 */
public class JsrValidityImpl implements Validity {

    private final Set<? extends ConstraintViolation<?>> violations;
    private final String object;
    private volatile String message;
    private final Object lock = new Object();

    public JsrValidityImpl(String object, Set<? extends ConstraintViolation<?>> violations) {
        Assert.hasText(object, "object is null or empty");
        this.object = object;
        Assert.notNull(violations, "violations is null");
        this.violations = violations;
    }

    @Override
    public String getObject() {
        return this.object;
    }

    @Override
    public boolean isValid() {
        return violations.isEmpty();
    }

    @Override
    public String getMessage() {
        if(message == null) {
            synchronized (lock) {
                if(message == null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("'").append(this.object).append("' properties: ");
                    boolean first = true;
                    for(ConstraintViolation<?> violation: violations) {
                        if(first) {
                            first = false;
                        } else {
                            sb.append(", ");
                        }
                        sb.append(violation.getPropertyPath().toString()).append(" - ");
                        sb.append(violation.getMessage());
                    }
                    message = sb.toString();
                }
            }
        }
        return message;
    }
}
