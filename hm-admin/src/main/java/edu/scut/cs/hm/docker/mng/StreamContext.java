package edu.scut.cs.hm.docker.mng;

import com.google.common.util.concurrent.SettableFuture;
import lombok.Data;

import java.io.InputStream;
import java.util.function.Consumer;

@Data
public class StreamContext<T> {

    private final InputStream stream;
    private final Consumer<T> watcher;
    private final SettableFuture<Boolean> interrupter = SettableFuture.create();

    public void interrupt() {
        interrupter.set(Boolean.TRUE);
    }
}
