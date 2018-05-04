package edu.scut.cs.hm.model.ngroup;

import edu.scut.cs.hm.common.kv.mapping.KvMapping;
import edu.scut.cs.hm.common.utils.Cloneables;
import edu.scut.cs.hm.docker.DockerConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class DockerClusterConfig extends AbstractNodesGroupConfig<DockerClusterConfig> implements DockerBasedClusterConfig{
    @KvMapping
    private DockerConfig config;

    @KvMapping
    private int swarmPort = 4375;

    /**
     * List of managers nodes.
     */
    @NotNull
    @Size(min = 1)
    @KvMapping
    private List<String> managers;

    @Override
    public DockerClusterConfig clone() {
        DockerClusterConfig clone = super.clone();
        clone.managers = Cloneables.clone(clone.managers);
        return clone;
    }
}
