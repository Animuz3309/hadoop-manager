package edu.scut.cs.hm.common.mb;

/**
 * Info part of MessageBus interface
 */
public interface MessageBusInfo<M> {

    /**
     * Id of bus. It unique string which help to identity bus in app environment. <p/>
     * Can be used in error handlers.
     */
    String getId();

    /**
     * Type of buss messages.
     */
    Class<M> getType();
}
