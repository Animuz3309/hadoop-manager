package edu.scut.cs.hm.admin.web.api;

import edu.scut.cs.hm.admin.web.model.UiUtils;
import edu.scut.cs.hm.admin.web.model.volume.UiVolume;
import edu.scut.cs.hm.docker.DockerService;
import edu.scut.cs.hm.docker.arg.DeleteUnusedVolumesArg;
import edu.scut.cs.hm.docker.arg.GetVolumesArg;
import edu.scut.cs.hm.docker.arg.RemoveVolumeArg;
import edu.scut.cs.hm.docker.cmd.CreateVolumeCmd;
import edu.scut.cs.hm.docker.model.volume.Volume;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.model.ExtendedAssert;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import edu.scut.cs.hm.model.ngroup.NodesGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/volumes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class VolumeApi {

    private final DiscoveryStorage discoveryStorage;

    private DockerService getDocker(@RequestParam("cluster") String clusterName) {
        NodesGroup cluster = discoveryStorage.getCluster(clusterName);
        ExtendedAssert.notFound(cluster, "Can not find cluster: " + clusterName);
        return cluster.getDocker();
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public Collection<UiVolume> list(@RequestParam("cluster") String clusterName) {
        DockerService docker = getDocker(clusterName);
        List<Volume> volumes = docker.getVolumes(new GetVolumesArg());
        return volumes.stream().map(UiVolume::from).collect(Collectors.toList());
    }

    @RequestMapping(path = "/get", method = RequestMethod.GET)
    public UiVolume get(@RequestParam("cluster") String clusterName,
                        @RequestParam("volume") String volumeName) {
        DockerService docker = getDocker(clusterName);
        Volume volume = docker.getVolume(volumeName);
        ExtendedAssert.notFound(volume, "No volumes with name: " + volumeName);
        return UiVolume.from(volume);
    }

    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public UiVolume create(@RequestParam("cluster") String clusterName,
                           @RequestBody UiVolume src) {
        DockerService docker = getDocker(clusterName);
        CreateVolumeCmd arg = new CreateVolumeCmd();
        arg.setName(src.getName());
        arg.setDriver(src.getDriver());
        arg.setDriverOpts(src.getOptions());
        arg.setLabels(src.getLabels());
        Volume volume = docker.createVolume(arg);
        ExtendedAssert.error(volume != null, "Create of volumes with name: " + src.getName() + " return null");
        return UiVolume.from(volume);
    }

    @RequestMapping(path = "/delete", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@RequestParam("cluster") String clusterName,
                                    @RequestParam("volume") String volume,
                                    @RequestParam(value = "force", required = false) Boolean force) {
        DockerService docker = getDocker(clusterName);
        RemoveVolumeArg arg = new RemoveVolumeArg();
        arg.setName(volume);
        arg.setForce(force);
        ServiceCallResult scr = docker.removeVolume(arg);
        return UiUtils.createResponse(scr);
    }

    @RequestMapping(path = "/delete-unused", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUnused(@RequestParam("cluster") String clusterName) {
        DockerService docker = getDocker(clusterName);
        DeleteUnusedVolumesArg arg = new DeleteUnusedVolumesArg();
        ServiceCallResult scr = docker.deleteUnusedVolumes(arg);
        return UiUtils.createResponse(scr);
    }
}
