package edu.scut.cs.hm.agent.notifier;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 {
    # cpu load 1.0 - 100%, .5 - 50% and etc (float)
    'cpuLoad': 0.0,
    # memory in bytes (float)
    'memory': {
    'total': 0.0,
    'available': 0.0,
    'used': 0.0
    },
    # disk usage for each mount point, in bytes, (float)
    'disks': {
    '/': {'total': 0.0, 'used': 0.0},
    '/home': {'total': 0.0, 'used': 0.0},
    },
    'net': {
    'eth0': {'bytesOut': 0.0, 'bytesIn': 0.0}
    }
 }
 */
@Data
public class SysInfo {

    @Data
    public static class Disk {
        private long total;
        private long used;
    }

    @Data
    public static class Memory {
        private long total;
        private long used;
        private long available;
    }

    @Data
    public static class Net {
        private long bytesIn;
        private long bytesOut;
    }

    private float cpuLoad;
    private Memory memory;
    private final Map<String, Disk> disks = new HashMap<>();
    private final Map<String, Net> net = new HashMap<>();
}
