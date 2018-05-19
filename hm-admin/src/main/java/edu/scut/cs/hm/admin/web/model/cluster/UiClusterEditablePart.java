package edu.scut.cs.hm.admin.web.model.cluster;

import edu.scut.cs.hm.common.utils.Keeper;
import edu.scut.cs.hm.common.utils.Sugar;
import edu.scut.cs.hm.docker.DockerConfig;
import edu.scut.cs.hm.model.ngroup.DockerClusterConfig;
import edu.scut.cs.hm.model.ngroup.NodesGroupConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class UiClusterEditablePart {
    private final Keeper<String> title = new Keeper<>();
    private final Keeper<String> description = new Keeper<>();
    /**
     * SpEL string which applied to images. It evaluated over object with 'tag(name)' and 'label(key, val)' functions,
     * also it has 'r(regexp)' function which can combined with other, like: <code>'spel:tag(r(".*_dev")) or label("dev", "true")'</code>.
     */
    @ApiModelProperty("SpEL string which applied to images. It evaluated over object with 'tag(name)' and 'label(key, val)' functions,\n" +
            "also it has 'r(regexp)' function which can combined with other, like: <code>'spel:tag(r(\".*_dev\")) or label(\"dev\", \"true\")'</code>.")
    private final Keeper<String> filter = new Keeper<>();

    @ApiModelProperty("One of 'DEFAULT' (just a group of nodes), 'SWARM' (standalone swarm cluster), 'DOCKER' (docker in swarm-mode cluster). ")
    private String type;

    private DockerConfig.Builder config;
    @ApiModelProperty("(Just for cluster 'DOCKER') List of node names which is will be joined into cluster as managers.")
    private List<String> managers;
    @ApiModelProperty("(Just for cluster 'DOCKER') The swarm listening port ")
    private int swarmPort = -1;

    public void toCluster(NodesGroupConfig ng) {
        Sugar.setIfChanged(ng::setDescription, getDescription());
        Sugar.setIfChanged(ng::setTitle, getTitle());
        Sugar.setIfChanged(ng::setImageFilter, getFilter());
        if(ng instanceof DockerClusterConfig) {
            DockerClusterConfig dcc = (DockerClusterConfig) ng;
            dcc.setManagers(managers);
            if (swarmPort != -1) {
                dcc.setSwarmPort(swarmPort);
            }
        }
    }
}
