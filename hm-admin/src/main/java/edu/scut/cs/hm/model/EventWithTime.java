package edu.scut.cs.hm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Any event with time in ms (it used for sorting and filtering in history)
 */
public interface EventWithTime {

    /**
     * event occure time
     */
    @JsonIgnore
    long getTimeInMilliseconds();
}
