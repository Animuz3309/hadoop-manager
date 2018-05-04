package edu.scut.cs.hm.docker.model.mount;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.scut.cs.hm.common.utils.Cloneables;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@JsonSubTypes({
        @JsonSubTypes.Type(value = MountSource.BindSource.class, name = "bind"),
        @JsonSubTypes.Type(value = MountSource.VolumeSource.class, name = "volume"),
        @JsonSubTypes.Type(value = MountSource.TmpfsSource.class, name = "tmpfs")
})
@JsonPropertyOrder({"type", "source", "target"})
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true, property = "type")
public class MountSource<T extends MountSource<T>> implements Cloneable {

    private Mount.Type type;
    /**
     * Source specifies the name of the mount. Depending on mount type, this
     * may be a volume name or a host path, or even ignored.
     * Source is not supported for tmpfs (must be an empty value)
     */
    private String source;
    private String target;
    private boolean readonly;

    @Override
    @SuppressWarnings("unchecked")
    public T clone() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class BindSource extends MountSource<BindSource> {
        private Mount.Propagation propagation;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class VolumeSource extends MountSource<VolumeSource> {

        private boolean noCopy;

        @Setter(AccessLevel.NONE)
        private Map<String, String> labels = new HashMap<>();

        private String driver;

        @Setter(AccessLevel.NONE)
        private Map<String, String> driverOpts = new HashMap<>();

        @Override
        public VolumeSource clone() {
            VolumeSource clone = super.clone();
            clone.labels = Cloneables.clone(clone.labels);
            clone.driverOpts = Cloneables.clone(clone.driverOpts);
            return clone;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TmpfsSource extends MountSource<TmpfsSource> {
        /**
         * Size sets the size of the tmpfs, in bytes.
         */
        private long size;
        /**
         * Unix mode (permissions) of the tmpfs upon creation
         */
        private int mode;
    }
}
