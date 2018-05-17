package edu.scut.cs.hm.model.node;

import edu.scut.cs.hm.model.EventWithTime;
import edu.scut.cs.hm.model.WithCluster;
import lombok.Data;
import org.springframework.util.Assert;

import java.time.ZonedDateTime;

/**
 * Event of node Health
 */
@Data
public class NodeHealthEvent implements EventWithTime, WithCluster {
    private final String name;
    private final String cluster;
    private final NodeMetrics health;

    public NodeHealthEvent(String name, String cluster, NodeMetrics health) {
        Assert.notNull(name, "name is null");
        this.name = name;
        this.cluster = cluster; //ngroup can be null
        Assert.notNull(health, "health is null");
        this.health = health;
    }

    @Override
    public long getTimeInMilliseconds() {
        ZonedDateTime time = health.getTime();
        if(time == null) {
            return Long.MIN_VALUE;
        }
        return time.toEpochSecond();
    }
}
