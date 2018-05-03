package edu.scut.cs.hm.docker.arg;

import com.google.common.util.concurrent.SettableFuture;
import edu.scut.cs.hm.docker.model.events.DockerEvent;
import edu.scut.cs.hm.model.WithInterrupter;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Builder
@Data
public class GetEventsArg implements WithInterrupter {
    private final Consumer<DockerEvent> watcher;

    private final Map<String, List<String>> filters;
    // service accept time in specific string format, we must not deliver this to user
    private final Long since;

    private final Long until;

    private final SettableFuture<Boolean> interrupter = SettableFuture.create();
}
