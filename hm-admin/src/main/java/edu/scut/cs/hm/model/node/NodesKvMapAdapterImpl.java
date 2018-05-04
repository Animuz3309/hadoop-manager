package edu.scut.cs.hm.model.node;

import edu.scut.cs.hm.admin.service.NodeStorage;
import edu.scut.cs.hm.common.kv.mapping.KvMapAdapter;

public class NodesKvMapAdapterImpl implements KvMapAdapter<NodeRegistrationImpl> {
    private NodeStorage nodeStorage;

    public NodesKvMapAdapterImpl(NodeStorage nodeStorage) {
        this.nodeStorage = nodeStorage;
    }

    @Override
    public Object get(String key, NodeRegistrationImpl source) {
        return NodeInfoImpl.builder(source.getNodeInfo());
    }

    @Override
    public NodeRegistrationImpl set(String key, NodeRegistrationImpl source, Object value) {
        NodeInfo ni = (NodeInfo) value;
        if (source == null) {
            NodeInfoImpl.Builder nib = NodeInfoImpl.builder(ni);
            nib.setName(key);
            source = nodeStorage.newRegistration(nib);
        } else {
            source.updateNodeInfo(b -> {
                b.address(ni.getAddress());
                b.setCluster(ni.getCluster());
                b.setIdInCluster(ni.getIdInCluster());
                b.setLabels(ni.getLabels());
            });
        }
        return source;
    }

    @Override
    public Class<?> getType(NodeRegistrationImpl source) {
        return NodeInfoImpl.Builder.class;
    }
}
