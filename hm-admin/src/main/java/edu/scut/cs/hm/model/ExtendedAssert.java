package edu.scut.cs.hm.model;

import edu.scut.cs.hm.common.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

/**
 * Extend Assert
 */
public final class ExtendedAssert {
    private ExtendedAssert() {}

    /**
     * Throw 'not found' exception if object is null.
     * @param object
     * @param message
     */
    public static void notFound(Object object, String message) {
        if (object == null) {
            throw new NotFoundException(message);
        }
    }

    /**
     * Throw 'not found' exception if object is null.
     * @param object
     */
    public static void notFound(Object object) {
        notFound(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    /**
     * Throw {@link edu.scut.cs.hm.model.HttpException} with {@link org.springframework.http.HttpStatus#BAD_REQUEST }
     * when <code>ifFalse</code> is false
     * @param ifFalse condition
     * @param message template for {@link MessageFormat} or simple text when no args
     * @param args
     */
    public static void badRequest(boolean ifFalse, String message, Object... args) {
        genericCode(HttpStatus.BAD_REQUEST, ifFalse, message, args);
    }

    /**
     * Throw {@link edu.scut.cs.hm.model.HttpException} with {@link org.springframework.http.HttpStatus#INTERNAL_SERVER_ERROR }
     * when <code>ifFalse</code> is false
     * @param ifFalse condition
     * @param message template for {@link MessageFormat} or simple text when no args
     * @param args
     */
    public static void error(boolean ifFalse, String message, Object... args) {
        genericCode(HttpStatus.INTERNAL_SERVER_ERROR, ifFalse, message, args);
    }

    private static void genericCode(HttpStatus code, boolean ifFalse, String message, Object[] args) {
        if(ifFalse) {
            return;
        }
        String msg = format(message, args);
        throw new HttpException(code, msg);
    }

    private static String format(String message, Object[] args) {
        if(ArrayUtils.isEmpty(args)) {
            return message;
        }
        String msg;
        try {
            msg = MessageFormat.format(message, args);
        } catch (IllegalArgumentException e) {
            msg = message;
        }
        return msg;
    }

    /**
     * @see StringUtils#matchAz09Hyp(String)
     * @param str string
     * @param thatIt that it string represent
     */
    public static void matchAz09Hyp(String str, String thatIt) {
        badRequest(str != null && StringUtils.matchAz09Hyp(str),
                "The \"{0}\" is not match with '[A-Za-z0-9-]+': \"{1}\"", thatIt, str);
    }

    /**
     * @see StringUtils#matchId(String)
     * @param str string
     * @param thatIt that it string represent
     */
    public static void matchId(String str, String thatIt) {
        badRequest(str != null && StringUtils.matchId(str),
                "The \"{0}\" is not match with '[A-Za-z0-9-_:.]+': \"{1}\"", thatIt, str);
    }
}
