package edu.scut.cs.hm.docker;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import edu.scut.cs.hm.common.utils.Smelter;
import edu.scut.cs.hm.docker.model.swarm.Strategies;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for docker service api config.
 */
@Data
public class DockerConfig {

    private static final DockerConfig.Builder DEFAULT = DockerConfig.builder();

    @Data
    public static class Builder {
        /**
         * docker/swarm 'http[s]://host:port'
         */
        private String host;
        private String cluster;
        private int maxCountOfInstances = 1;
        private String dockerRestart;
        /**
         * Time in seconds, which data was cached after last write.
         */
        private long cacheTimeAfterWrite = 10L;
        private int dockerTimeout = 5 * 60;
        /**
         * Name of registries
         * @return
         */
        private final List <String> registries = new ArrayList<>();
        private Strategies strategy = Strategies.DEFAULT;

        public Builder from(DockerConfig orig) {
            if(orig == null) {
                return this;
            }
            setHost(orig.getHost());
            setMaxCountOfInstances(orig.getMaxCountOfInstances());
            setDockerRestart(orig.getDockerRestart());
            setCluster(orig.getCluster());
            setRegistries(orig.getRegistries());
            setCacheTimeAfterWrite(orig.getCacheTimeAfterWrite());
            setDockerTimeout(orig.getDockerTimeout());
            return this;
        }

        public Builder merge(DockerConfig.Builder src) {
            if(src == null) {
                return this;
            }
            Smelter<DockerConfig.Builder> s = new Smelter<>(src, DEFAULT);
            s.set(this::setHost, DockerConfig.Builder::getHost);
            s.setInt(this::setMaxCountOfInstances, DockerConfig.Builder::getMaxCountOfInstances);
            s.set(this::setDockerRestart, DockerConfig.Builder::getDockerRestart);
            s.set(this::setCluster, DockerConfig.Builder::getCluster);
            s.set(this::setRegistries, DockerConfig.Builder::getRegistries);
            s.setLong(this::setCacheTimeAfterWrite, DockerConfig.Builder::getCacheTimeAfterWrite);
            s.setInt(this::setDockerTimeout, DockerConfig.Builder::getDockerTimeout);
            return this;
        }

        public Builder host(String host) {
            setHost(host);
            return this;
        }

        public Builder cluster(String cluster) {
            setCluster(cluster);
            return this;
        }

        public Builder maxCountOfInstances(int maxCountOfInstances) {
            setMaxCountOfInstances(maxCountOfInstances);
            return this;
        }

        public Builder dockerRestart(String dockerRestart) {
            setDockerRestart(dockerRestart);
            return this;
        }

        public Builder cacheTimeAfterWrite(long cacheTimeAfterWrite) {
            setCacheTimeAfterWrite(cacheTimeAfterWrite);
            return this;
        }

        public Builder dockerTimeout(int dockerTimeout) {
            setDockerTimeout(dockerTimeout);
            return this;
        }

        public Builder addRegistry(String registry) {
            this.registries.add(registry);
            return this;
        }

        public Builder registries(List<String> registries) {
            setRegistries(registries);
            return this;
        }

        public void setRegistries(List<String> registries) {
            this.registries.clear();
            if (registries != null) {
                this.registries.addAll(registries);
            }
        }

        public Builder strategy(Strategies strategy) {
            this.strategy = strategy;
            return this;
        }

        public DockerConfig build() {
            return new DockerConfig(this);
        }
    }

    /**
     * docker/swarm 'host:port'
     */
    private final String host;
    private final String cluster;
    private final int maxCountOfInstances;
    private final String dockerRestart;
    /**
     * Time in seconds, which data was cached after last write.
     */
    private final long cacheTimeAfterWrite;
    private final int dockerTimeout;
    /**
     * Name of registries
     * @return
     */
    private final List<String> registries;
    private final Strategies strategy;

    public static Builder builder(DockerConfig cc) {
        return builder().from(cc);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public DockerConfig(Builder builder) {
        this.host = builder.host;
        this.cluster = builder.cluster;
        this.maxCountOfInstances = builder.maxCountOfInstances;
        this.dockerRestart = builder.dockerRestart;
        this.cacheTimeAfterWrite = builder.cacheTimeAfterWrite;
        this.dockerTimeout = builder.dockerTimeout;
        this.registries = ImmutableList.copyOf(builder.registries);
        this.strategy = builder.strategy;
    }

    public DockerConfig validate() {
        Assert.hasText(this.host, "Hosts is empty or null");
        Assert.isTrue(this.host.contains(":"), "Hosts does not has port: " + this.host);
        return this;
    }
}
