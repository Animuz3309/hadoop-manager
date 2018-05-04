package edu.scut.cs.hm.model.cluster;

import edu.scut.cs.hm.admin.component.SwarmProcesses;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Function which create swarm discovery url
 */
public interface SwarmDiscoveryUrlFunction {

    @ToString
    final class Etcd implements SwarmDiscoveryUrlFunction {

        private final String etcds;

        public Etcd(List<String> etcdAddreses) {
            this.etcds = StringUtils.collectionToCommaDelimitedString(etcdAddreses);
        }

        @Override
        public String supply(SwarmProcesses.SwarmProcess proc) {
            return "etcd://" + this.etcds + "/discovery/" + proc.getCluster();
        }
    }

    String supply(SwarmProcesses.SwarmProcess proc);
}
