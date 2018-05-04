package edu.scut.cs.hm.model.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static edu.scut.cs.hm.common.utils.Sugar.setIfNotNull;

/**
 * Health data and statistics about node.
 */
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)    // for info we want to see all fields include nulls
public class NodeMetrics {

    @Data
    public static class Builder {
        private ZonedDateTime time;
        private Boolean healthy;
        private Boolean manager;
        private State state;
        private Long swarmMemReserved;
        private Long swarmMemTotal;
        private Integer swarmCpusReserved;
        private Integer swarmCpusTotal;
        private Long sysMemAvail;
        private Long sysMemTotal;
        private Long sysMemUsed;
        private Float sysCpuLoad;
        private final Map<String, DiskInfo> disks = new HashMap<>();
        private final Map<String, NetIfaceCounter> net = new HashMap<>();

        public Builder from(NodeMetrics metric) {
            if(metric == null) {
                return this;
            }
            setTime(metric.getTime());
            setHealthy(metric.getHealthy());
            setManager(metric.getManager());
            setState(metric.getState());
            setSwarmMemReserved(metric.getSwarmMemReserved());
            setSwarmMemTotal(metric.getSwarmMemTotal());
            setSwarmCpusReserved(metric.getSwarmCpusReserved());
            setSwarmCpusTotal(metric.getSwarmCpusTotal());
            setSysMemAvail(metric.getSysMemAvail());
            setSysMemTotal(metric.getSysMemTotal());
            setSysMemUsed(metric.getSysMemUsed());
            setSysCpuLoad(metric.getSysCpuLoad());
            setDisks(metric.getDisks());
            setNet(metric.getNet());
            return this;
        }

        public Builder fromNonNull(NodeMetrics metrics) {
            if (metrics == null) {
                return this;
            }
            // choose latest time
            ZonedDateTime time = getTime();
            ZonedDateTime newTime = metrics.getTime();
            if(time == null || newTime != null && newTime.isAfter(time)) {
                setTime(newTime);
            }
            setIfNotNull(this::setHealthy, metrics.getHealthy());
            setIfNotNull(this::setManager, metrics.getManager());
            setIfNotNull(this::setState, metrics.getState());
            setIfNotNull(this::setSwarmMemReserved, metrics.getSwarmMemReserved());
            setIfNotNull(this::setSwarmMemTotal, metrics.getSwarmMemTotal());
            setIfNotNull(this::setSwarmCpusReserved, metrics.getSwarmCpusReserved());
            setIfNotNull(this::setSwarmCpusTotal, metrics.getSwarmCpusTotal());
            setIfNotNull(this::setSysMemAvail, metrics.getSysMemAvail());
            setIfNotNull(this::setSysMemTotal, metrics.getSysMemTotal());
            setIfNotNull(this::setSysMemUsed, metrics.getSysMemUsed());
            setIfNotNull(this::setSysCpuLoad, metrics.getSysCpuLoad());
            Map<String, DiskInfo> disks = metrics.getDisks();
            if(!CollectionUtils.isEmpty(disks)) {
                this.setDisks(disks);
            }
            Map<String, NetIfaceCounter> net = metrics.getNet();
            if(!CollectionUtils.isEmpty(net)) {
                this.setNet(net);
            }
            return this;
        }

        public Builder time(ZonedDateTime time) {
            setTime(time);
            return this;
        }

        public Builder healthy(Boolean healthy) {
            setHealthy(healthy);
            return this;
        }

        public Builder manager(Boolean master) {
            setManager(master);
            return this;
        }

        public Builder state(State state) {
            setState(state);
            return this;
        }

        public Builder swarmMemReserved(Long swarmMemReserved) {
            setSwarmMemReserved(swarmMemReserved);
            return this;
        }

        public Builder swarmMemTotal(Long swarmMemTotal) {
           setSwarmMemTotal(swarmMemTotal);
            return this;
        }

        public Builder swarmCpusReserved(Integer swarmCpusReserved) {
            setSwarmCpusReserved(swarmCpusReserved);
            return this;
        }

        public Builder swarmCpusTotal(Integer swarmCpusTotal) {
            setSwarmCpusTotal(swarmCpusTotal);
            return this;
        }

        public Builder sysMemAvail(Long sysMemAvail) {
           setSysMemAvail(sysMemAvail);
            return this;
        }

        public Builder sysMemTotal(Long sysMemTotal) {
            setSysMemTotal(sysMemTotal);
            return this;
        }

        public Builder sysMemUsed(Long sysMemUsed) {
            setSysMemUsed(sysMemUsed);
            return this;
        }

        public Builder sysCpuLoad(Float sysCpuLoad) {
            setSysCpuLoad(sysCpuLoad);
            return this;
        }

        public Builder addDisk(DiskInfo diskInfo) {
            disks.put(diskInfo.getMount(), diskInfo);
            return this;
        }

        public void setDisks(Map<String, DiskInfo> disks) {
            this.disks.clear();
            if (disks != null) {
                this.disks.putAll(disks);
            }
        }

        public Builder addNet(NetIfaceCounter net) {
            this.net.put(net.getName(), net);
            return this;
        }

        public void setNet(Map<String, NetIfaceCounter> net) {
            this.net.clear();
            if(net != null) {
                this.net.putAll(net);
            }
        }

        public NodeMetrics build() {
            return new NodeMetrics(this);
        }
    }

    private final ZonedDateTime time;
    private final Boolean healthy;
    private final Boolean manager;
    private final State state;
    private final Long swarmMemReserved;
    private final Long swarmMemTotal;
    private final Integer swarmCpusReserved;
    private final Integer swarmCpusTotal;
    private final Long sysMemAvail;
    private final Long sysMemTotal;
    private final Long sysMemUsed;
    private final Float sysCpuLoad;
    private final Map<String, DiskInfo> disks;
    private final Map<String, NetIfaceCounter> net;

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public NodeMetrics(Builder b) {
        this.time = b.time;
        this.healthy = b.healthy;
        this.manager = b.manager;
        this.state = b.state;
        this.swarmMemReserved = b.swarmMemReserved;
        this.swarmMemTotal = b.swarmMemTotal;
        this.swarmCpusReserved = b.swarmCpusReserved;
        this.swarmCpusTotal = b.swarmCpusTotal;
        this.sysMemAvail = b.sysMemAvail;
        this.sysMemTotal = b.sysMemTotal;
        this.sysMemUsed = b.sysMemUsed;
        this.sysCpuLoad = b.sysCpuLoad;
        this.disks = ImmutableMap.copyOf(b.disks);
        this.net = ImmutableMap.copyOf(b.net);
    }

    public enum State {
        PENDING,    // if node is on but not inside cluster yet -> set status pending
        UNHEALTHY,
        HEALTHY,
        DISCONNECTED,
    }
}
