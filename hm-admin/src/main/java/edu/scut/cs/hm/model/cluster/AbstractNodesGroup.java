package edu.scut.cs.hm.model.cluster;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public abstract class AbstractNodesGroup<C extends AbstractNodesGroupConfig<C>> implements NodesGroup, AutoCloseable {

}
