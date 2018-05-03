package edu.scut.cs.hm.model;

import com.google.common.util.concurrent.SettableFuture;

public interface WithInterrupter {
    /**
     * Handle for interrupt process
     * @return
     */
    SettableFuture<Boolean> getInterrupter();
}
