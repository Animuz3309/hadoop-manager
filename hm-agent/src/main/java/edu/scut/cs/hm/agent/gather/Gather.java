package edu.scut.cs.hm.agent.gather;

import edu.scut.cs.hm.agent.notifier.SysInfo;

/**
 * Interface for gather information from system
 */
public interface Gather {
    void fill(SysInfo info) throws Exception;
}
