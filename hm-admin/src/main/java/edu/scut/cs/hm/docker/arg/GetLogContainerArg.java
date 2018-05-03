package edu.scut.cs.hm.docker.arg;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.SettableFuture;
import edu.scut.cs.hm.docker.res.ProcessEvent;
import edu.scut.cs.hm.model.WithInterrupter;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.function.Consumer;

@Builder
@Data
public class GetLogContainerArg implements WithInterrupter {

    /**
     * Container ID
     */
    private final String id;

    private final Consumer<ProcessEvent> watcher;

    /**
     * show stdout log. Default true
     */
    private boolean stdout = true;
    /**
     * show stderr log. Default true
     */
    private boolean stderr = true;
    /**
     * return stream. Default false
     */
    private boolean follow = false;
    /**
     * timestamp to filter logs. Specifying a timestamp will only
     *  output log-entries since that timestamp.
     */
    private final Date since;
    /**
     * print timestamps for every log line. Default false.
     */
    private boolean timestamps = false;
    /**
     * Output specified number of lines at the end of logs: all or <number>.
     */
    private final Integer tail;

    private final SettableFuture<Boolean> interrupter = SettableFuture.create();

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("watcher", watcher)
                .add("stdout", stdout)
                .add("stderr", stderr)
                .add("follow", follow)
                .add("since", since)
                .add("timestamps", timestamps)
                .add("tail", tail)
                .omitNullValues()
                .toString();
    }
}
