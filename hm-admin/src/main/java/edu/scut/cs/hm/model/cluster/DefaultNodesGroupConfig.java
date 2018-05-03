package edu.scut.cs.hm.model.cluster;

import com.fasterxml.jackson.annotation.JsonCreator;
import edu.scut.cs.hm.common.kv.mapping.KvMapping;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultNodesGroupConfig extends AbstractNodesGroupConfig<DefaultNodesGroupConfig> {

    @KvMapping
    private String nodeFilter;

    @JsonCreator
    public DefaultNodesGroupConfig() {
    }

    public DefaultNodesGroupConfig(String name, String nodeFilter) {
        setName(name);
        setNodeFilter(nodeFilter);
    }
}
