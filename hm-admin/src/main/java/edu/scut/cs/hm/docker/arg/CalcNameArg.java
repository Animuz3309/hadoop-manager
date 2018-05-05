package edu.scut.cs.hm.docker.arg;

import com.google.common.base.MoreObjects;
import edu.scut.cs.hm.docker.DockerService;
import lombok.Builder;
import lombok.Data;

/**
 * Parameters required for creating new container
 */
@Builder
@Data
public class CalcNameArg {

    /**
     * explicitly passed name has highest priority
     * value of containerName field in configurations (image labels, external configuration, etc) has second priority
     */
    private final String containerName;
    /**
     * name of Docker Image
     */
    private final String imageName;
    /**
     * allocate created name
     */
    private final boolean allocate;
    private final DockerService dockerService;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("containerName", containerName)
                .add("imageName", imageName)
                .add("allocate", allocate)
                .add("dockerService", dockerService)
                .omitNullValues()
                .toString();
    }
}
