package edu.scut.cs.hm.admin.web.model.cluster;

import edu.scut.cs.hm.admin.web.model.UiPermission;
import edu.scut.cs.hm.model.WithUiPermission;
import edu.scut.cs.hm.model.ngroup.NodeGroupState;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class UiCluster extends UiClusterEditablePart implements Comparable<UiCluster>, WithUiPermission {
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Entry {
        private int on;
        private int off;

        public void incrementOff() {
            off++;
        }

        public void incrementOn() {
            on++;
        }
    }

    private String name;
    private Set<NodesGroup.Feature> features;
    private Entry nodes;
    private Entry containers;
    private UiPermission permission;
    private NodeGroupState state;
    private Set<String> applications = new HashSet<>();

    @Override
    public int compareTo(UiCluster o) {
        if (o == null) {
            return 1;
        }
        return ObjectUtils.compare(this.name, o.name);
    }
}
