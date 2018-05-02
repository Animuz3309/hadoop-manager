package edu.scut.cs.hm.model;

/**
 * Action over object <p/>
 * Usually event with action.
 */
public interface WithAction {
    /**
     * Action name
     * @see StandardAction
     * @return
     */
    Object getAction();
}
