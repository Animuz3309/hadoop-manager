package edu.scut.cs.hm.model.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import edu.scut.cs.hm.common.kv.mapping.KvMapping;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@KvMapping
@Data
public class ApplicationImpl implements Application {

    @Data
    public static class Builder {
        private String name;
        private String cluster;
        private String initFile;
        private Date creatingDate;
        private final List<String> containers = new ArrayList<>();

        public Builder name(String name) {
            setName(name);
            return this;
        }

        public Builder cluster(String cluster) {
            setCluster(cluster);
            return this;
        }

        public Builder initFile(String initFile) {
            setInitFile(initFile);
            return this;
        }

        public Builder creatingDate(Date creatingDate) {
            setCreatingDate(creatingDate);
            return this;
        }

        public Builder containers(List<String> containers) {
            setContainers(containers);
            return this;
        }

        public void setContainers(List<String> containers) {
            this.containers.clear();
            if(containers != null) {
                this.containers.addAll(containers);
            }
        }

        public Builder from(Application application) {
            setName(application.getName());
            setCluster(application.getCluster());
            setInitFile(application.getInitFile());
            setCreatingDate(application.getCreatingDate());
            setContainers(application.getContainers());
            return this;
        }

        public ApplicationImpl build() {
            return new ApplicationImpl(this);
        }
    }

    private final String name;
    private final String cluster;
    private final String initFile;
    private final Date creatingDate;
    private final List<String> containers;

    @JsonCreator
    public ApplicationImpl(Builder b) {
        this.name = b.name;
        this.cluster = b.cluster;
        this.initFile = b.initFile;
        this.creatingDate = b.creatingDate;
        this.containers = ImmutableList.copyOf(b.containers);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<String> getContainers() {
        return containers;
    }

    public static ApplicationImpl from(Application application) {
        if(application instanceof ApplicationImpl) {
            return (ApplicationImpl) application;
        }
        return ApplicationImpl.builder().from(application).build();
    }
}
