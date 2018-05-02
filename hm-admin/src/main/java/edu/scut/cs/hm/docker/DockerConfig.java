package edu.scut.cs.hm.docker;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import edu.scut.cs.hm.common.utils.Smelter;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for docker service api config.
 * TODO docker strategies "random binpack spread"
 */
@Data
public class DockerConfig {

    private static final DockerConfig DEFAULT = DockerConfig.builder().build();

    @Data
    public static class Builder {
        /**
         * docker/swarm 'http[s]://host:port'
         */
        private String host;
        private int maxCountOfInstances = 1;
        private String dockerRestart;
        private String cluster;
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

        public Builder merge(DockerConfig src) {
            if(src == null) {
                return this;
            }
            Smelter<DockerConfig> s = new Smelter<>(src, DEFAULT);
            s.set(this::setHost, DockerConfig::getHost);
            s.setInt(this::setMaxCountOfInstances, DockerConfig::getMaxCountOfInstances);
            s.set(this::setDockerRestart, DockerConfig::getDockerRestart);
            s.set(this::setCluster, DockerConfig::getCluster);
            s.set(this::setRegistries, DockerConfig::getRegistries);
            s.setLong(this::setCacheTimeAfterWrite, DockerConfig::getCacheTimeAfterWrite);
            s.setInt(this::setDockerTimeout, DockerConfig::getDockerTimeout);
            return this;
        }

        public Builder host(String host) {
            setHost(host);
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

        public DockerConfig build() {
            return new DockerConfig(this);
        }
    }

    /**
     * docker/swarm 'host:port'
     */
    private final String host;
    private final int maxCountOfInstances;
    private final String dockerRestart;
    private final String cluster;
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

    public static Builder builder(DockerConfig cc) {
        return builder().from(cc);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonCreator
    public DockerConfig(Builder builder) {
        this.host = builder.host;
        this.maxCountOfInstances = builder.maxCountOfInstances;
        this.dockerRestart = builder.dockerRestart;
        this.cluster = builder.cluster;
        this.cacheTimeAfterWrite = builder.cacheTimeAfterWrite;
        this.dockerTimeout = builder.dockerTimeout;
        this.registries = ImmutableList.copyOf(builder.registries);
    }

    public DockerConfig validate() {
        Assert.hasText(this.host, "Hosts is empty or null");
        Assert.isTrue(this.host.contains(":"), "Hosts does not has port: " + this.host);
        return this;
    }
}
