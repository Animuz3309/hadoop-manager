package edu.scut.cs.hm.docker.arg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

/**
 * Argument for {@link edu.scut.cs.hm.docker.DockerService#killContainer(KillContainerArg)}
 */
@Builder
@Data
public class KillContainerArg {
    public enum Signal {
        SIGINT,
        SIGQUIT,
        SIGABRT,
        SIGKILL,
        SIGTERM,
        SIGTSTP,
        SIGSTOP,
        SIGCONT,
        SIGTTIN,
        SIGTTOU
    }

    /**
     * Container ID
     */
    @JsonIgnore
    private final String id;

    /**
     * default SIGKILL
     * @return
     */
    private final Signal signal;

}

