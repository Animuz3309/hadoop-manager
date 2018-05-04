package edu.scut.cs.hm.admin.config.configurer;

import edu.scut.cs.hm.docker.model.swarm.Strategies;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration of swarm executable
 */
@Data
@ConfigurationProperties("hm.swarm-exec")
public class SwarmProcessesConfig implements Cloneable {

    /**
     * Path to swarm executable. Optional.
     */
    private String path = "swarm";
    /**
     * Address which is used for binding of swarm.
     */
    private String address = "127.0.0.1";
    /**
     * Minimal value of swarm port
     */
    private int minPort = 2376;
    /**
     * Maximal value of swarm port
     */
    private int maxPort = 2976;
    /**
     * Directory for log of each running swarm process
     */
    private String logDir;
    /**
     * Max number of swarm processes. You interpret it at max number of clusters.
     */
    private int maxProcesses = 100;
    /**
     * Max time in seconds for waiting of process start. If process not started in this time then
     * it will be recognized as fallen.
     */
    private int maxWaitOnStart = 10;
    private Strategies strategy = Strategies.DEFAULT;

    @Override
    public SwarmProcessesConfig clone() {
        try {
            return (SwarmProcessesConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
