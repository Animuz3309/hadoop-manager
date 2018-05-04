package edu.scut.cs.hm.model.ngroup;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.scut.cs.hm.common.kv.mapping.KvMapping;
import edu.scut.cs.hm.common.security.acl.dto.AclSource;
import lombok.Data;

@JsonTypeInfo( // 增加json数据内的class标识
        use = JsonTypeInfo.Id.NAME,
        property = "groupType",
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DefaultNodesGroupConfig.class, name = NodesGroupConfig.TYPE_DEFAULT),
    @JsonSubTypes.Type(value = SwarmNodesGroupConfig.class, name = NodesGroupConfig.TYPE_SWARM),
    @JsonSubTypes.Type(value = DockerClusterConfig.class, name = NodesGroupConfig.TYPE_DOCKER),
})
@Data
public abstract class AbstractNodesGroupConfig<T extends AbstractNodesGroupConfig<T>> implements Cloneable, NodesGroupConfig {

    @KvMapping
    private String name;
    @KvMapping
    private String title;
    @KvMapping
    private String imageFilter;
    @KvMapping
    private String description;
    @KvMapping
    private String defaultNetwork;
    @KvMapping
    private AclSource acl;

    @SuppressWarnings("unchecked")
    @Override
    protected T clone() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
