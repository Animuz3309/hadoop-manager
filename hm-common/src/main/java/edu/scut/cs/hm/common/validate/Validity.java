package edu.scut.cs.hm.common.validate;

/**
 * Result of validation process
 */
public interface Validity {
    /**
     * String identifier (or something other that may identity object) of validated object.
     * @return
     */
    String getObject();

    boolean isValid();

    /**
     * Message wit details about invalidity of target.
     * @return
     */
    String getMessage();
}
