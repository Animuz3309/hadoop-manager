package edu.scut.cs.hm.model;

import edu.scut.cs.hm.common.utils.Cloneables;
import edu.scut.cs.hm.model.container.ContainerSource;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApplicationSource implements Cloneable, Comparable<ApplicationSource> {

    private String name;
    @Setter(AccessLevel.NONE)
    private List<ContainerSource> containers = new ArrayList<>();
    //TODO + networks
    //TODO + volumes


    @Override
    public ApplicationSource clone() {
        try {
            ApplicationSource clone = (ApplicationSource) super.clone();
            clone.containers = Cloneables.clone(clone.containers);
            // do not forget uncomment below
            //clone.networks = Cloneables.clone(clone.networks);
            //clone.volumes = Cloneables.clone(clone.volumes);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int compareTo(ApplicationSource o) {
        return ObjectUtils.compare(name, o.name);
    }
}
