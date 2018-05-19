package edu.scut.cs.hm.agent.gather;

import edu.scut.cs.hm.agent.notifier.SysInfo;
import edu.scut.cs.hm.common.utils.DataSize;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemGather implements Gather {
    private final Pattern pattern = Pattern.compile("\\w+:\\s+(\\d+)\\s+(\\w+)");
    private final Path meminfo;

    MemGather(String rootPath) {
        this.meminfo = Paths.get(rootPath, "proc/meminfo");
    }

    @Override
    public void fill(SysInfo info) throws Exception {
        try(BufferedReader br = Files.newBufferedReader(meminfo)) {
            /*
            * cat /proc/meminfo
                MemTotal:        8177820 kB
                MemFree:         2715188 kB
                MemAvailable:    4913576 kB
            */
            long total = parse(br.readLine());
            long free = parse(br.readLine());
            long avail = parse(br.readLine());
            SysInfo.Memory mem = new SysInfo.Memory();
            mem.setTotal(total);
            mem.setAvailable(free);
            // used is total - avail (which include free, unloadable file cache)
            mem.setUsed(total - avail);
            info.setMemory(mem);
        }
    }

    private long parse(String line) {
        Matcher matcher = pattern.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Can not match: '" + line + "' with " + pattern);
        }
        long val = Long.parseLong(matcher.group(1));
        String multStr = matcher.group(2);
        long mult = DataSize.parseMultiplier(multStr);
        return val * mult;
    }
}
