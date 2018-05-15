package edu.scut.cs.hm.agent.gather;

import com.google.common.base.Splitter;
import edu.scut.cs.hm.agent.notifier.SysInfo;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class StatGather implements Gather, Refreshable {
    private static class Data {
        final long user;
        final long nice;
        final long system;
        final long idle;
        final long iowait;

        Data(String line) {
            Iterator<String> iter = SPLITTER_ON_SPACE.split(line).iterator();
            /*
            line:
               cpu  2255 34 2290 22625563 6290 127 456
            descr:
                user: normal processes executing in user mode
                nice: niced processes executing in user mode
                system: processes executing in kernel mode
                idle: twiddling thumbs
                iowait: waiting for I/O to complete
                irq: servicing interrupts
                softirq: servicing softirqs
            */
            iter.next();//skip 'cpu'
            user = nextLong(iter);
            nice = nextLong(iter);
            system = nextLong(iter);
            idle = nextLong(iter);
            iowait = nextLong(iter);
        }
    }

    private static final Splitter SPLITTER_ON_SPACE = Splitter.on(' ').omitEmptyStrings().trimResults();
    private static long nextLong(Iterator<String> iter) {
        return Long.parseLong(iter.next());
    }

    private final Path stat;
    private volatile Data prev;
    private volatile Data curr;

    StatGather(String rootPath) {
        this.stat = Paths.get(rootPath, "proc/stat");
    }

    @Override
    public void refresh() throws Exception {
        try(BufferedReader r = Files.newBufferedReader(stat)) {
            String line = r.readLine();
            Data data = new Data(line);
            synchronized (this) {
                this.prev = this.curr;
                this.curr = data;
            }
        }
    }

    @Override
    public void fill(SysInfo info) {
        Data c;
        Data p;
        synchronized (this) {
            c = this.curr;
            p = this.prev;
        }
        if(p == null || c == null) {
            return;
        }
        float usage = (c.user - p.user) + (c.nice - p.nice) + (c.system - p.system);
        float idle = (c.idle - p.idle) + (c.iowait - p.iowait);
        float cpuLoad = 100f * usage/(usage + idle);
        info.setCpuLoad(cpuLoad);
    }
}
