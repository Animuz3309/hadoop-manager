package edu.scut.cs.hm.agent.gather;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import edu.scut.cs.hm.agent.notifier.SysInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SysInfoGather {
    private final List<Gather> gathers;

    /**
     * Create new instance of info collector.
     * @param rootPath path to mounter root, if not specified use '/'
     */
    public SysInfoGather(String rootPath) {
        rootPath= MoreObjects.firstNonNull(rootPath, "/");
        this.gathers = ImmutableList.of(
                new NetGather(rootPath),
                new MemGather(rootPath),
                new StatGather(rootPath));
    }

    /**
     * Get current info. Some Collectors may periodically gather info, therefore you must manually cal {@link #refresh()}
     * @see #refresh()
     * @return info
     */
    public SysInfo getSysInfo() {
        SysInfo sysInfo = new SysInfo();
        gathers.forEach(c -> safe(() -> c.fill(sysInfo)));
        return sysInfo;
    }

    /**
     * For proper work you need at least two invocation of this method, between {@link #getInfo()}.
     * Not that small (less than one second) timeout between invocation may cause incorrect results.
     */
    public void refresh() {
        gathers.forEach(c -> {
            if(!(c instanceof Refreshable)) {
                return;
            }
            safe(((Refreshable) c)::refresh);
        });
    }

    private void safe(UnsafeRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.error("Can not execute {}", runnable, e);
        }
    }
}
