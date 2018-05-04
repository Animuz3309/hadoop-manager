package edu.scut.cs.hm.docker.arg;

import com.google.common.util.concurrent.SettableFuture;
import edu.scut.cs.hm.docker.model.health.Statistics;
import edu.scut.cs.hm.model.WithInterrupter;
import lombok.Builder;
import lombok.Data;

import java.util.function.Consumer;

/**
 * Get statistics from docker service
 * @see edu.scut.cs.hm.docker.DockerService#getStatistics(GetStatisticsArg)
 */
@Builder(builderClassName = "Builder")
@Data
public class GetStatisticsArg implements WithInterrupter {
    /**
     * Container id
     */
    private final String id;
    private final boolean stream;
    private final SettableFuture<Boolean> interrupter = SettableFuture.create();
    private final Consumer<Statistics> watcher;
}
