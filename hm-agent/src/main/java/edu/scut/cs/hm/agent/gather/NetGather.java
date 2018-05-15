package edu.scut.cs.hm.agent.gather;

import edu.scut.cs.hm.agent.notifier.SysInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class NetGather implements Gather {
    private final Path path;

    NetGather(String rootPath) {
        // network interface dir
        this.path = Paths.get(rootPath, "sys/class/net/");
    }

    @Override
    public void fill(SysInfo info) throws Exception {
        // /sys/class/net/eth0/statistics/rx_bytes
        // /sys/class/net/eth0/statistics/tx_bytes
        Map<String, SysInfo.Net> nets = info.getNet();
        for(Path devPath: (Iterable<Path>)(Files.list(path)::iterator)) {
            String dev = devPath.getFileName().toString();
            SysInfo.Net net = new SysInfo.Net();
            readNet(devPath, net);
            nets.put(dev, net);
        }
    }

    private void readNet(Path dev, SysInfo.Net net) throws IOException {
        long rx = readLong(dev.resolve("statistics/rx_bytes"));
        long tx = readLong(dev.resolve("statistics/tx_bytes"));
        net.setBytesIn(rx);
        net.setBytesOut(tx);
    }

    private long readLong(Path path) throws IOException {
        return Long.parseLong(Files.readAllLines(path).get(0));
    }
}
