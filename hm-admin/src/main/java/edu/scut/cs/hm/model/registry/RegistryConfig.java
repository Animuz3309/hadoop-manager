package edu.scut.cs.hm.model.registry;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.MoreObjects;
import edu.scut.cs.hm.common.kv.mapping.KvMapping;
import lombok.Data;

/**
 * Configuration for registry service
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "registryType",
        defaultImpl = PrivateRegistryConfig.class,
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PrivateRegistryConfig.class, name = "PRIVATE"),
        @JsonSubTypes.Type(value = HubRegistryConfig.class, name = "DOCKER_HUB"),
})
@Data
public abstract class RegistryConfig implements Cloneable {

    /**
     * Unique identifier for registry. It must not be changed after adding registry.
     * <b>Also, it used as part of image name.<b/>
     */
    @KvMapping
    private String name;
    @KvMapping
    private String title;
    @KvMapping
    private boolean disabled;
    @KvMapping
    private boolean readOnly;
    //it not has kv mapping because non editable registries must be only in config
    private boolean editable = true;
    private String errorMessage;
    @KvMapping
    private RegistryType registryType;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                // we do not show password in to string because it can appear in logs
                .add("registryType", registryType)
                .add("disabled", disabled)
                .add("errorMessage", errorMessage)
                .add("title", title)
                .add("name", name)
                .omitNullValues()
                .toString();
    }

    public RegistryConfig clone() {
        try {
            return (RegistryConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void cleanCredentials();
}
