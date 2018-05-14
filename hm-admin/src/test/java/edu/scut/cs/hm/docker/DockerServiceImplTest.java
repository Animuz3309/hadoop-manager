package edu.scut.cs.hm.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.common.mb.MessageBus;
import edu.scut.cs.hm.docker.arg.*;
import edu.scut.cs.hm.docker.cmd.CreateContainerCmd;
import edu.scut.cs.hm.docker.model.container.ContainerDetails;
import edu.scut.cs.hm.docker.model.container.DockerContainer;
import edu.scut.cs.hm.docker.model.image.ImageDescriptor;
import edu.scut.cs.hm.docker.model.swarm.Service;
import edu.scut.cs.hm.docker.model.swarm.Task;
import edu.scut.cs.hm.docker.model.volume.Volume;
import edu.scut.cs.hm.docker.res.CreateContainerResponse;
import edu.scut.cs.hm.docker.res.ServiceCallResult;
import edu.scut.cs.hm.docker.res.SwarmInspectResponse;
import edu.scut.cs.hm.model.node.NodeInfoProvider;
import edu.scut.cs.hm.model.registry.HttpAuthInterceptor;
import edu.scut.cs.hm.model.registry.RegistryRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@SuppressWarnings("deprecation")
public class DockerServiceImplTest {

    private DockerServiceImpl service;

    @Before
    public void setUp() throws Exception {
        service = dockerService();
    }

    @SuppressWarnings("unchecked")
    private DockerServiceImpl dockerService() {
        DockerConfig config = DockerConfig.builder().host("localhost:2375").build();
        AsyncRestTemplate restTemplate = new AsyncRestTemplate();
        RegistryRepository registryRepository = mock(RegistryRepository.class);
        restTemplate.setInterceptors(
                Collections.singletonList(new HttpAuthInterceptor(registryRepository))
        );

        return DockerServiceImpl.builder()
                .config(config)
                .node("test")
                .restTemplate(restTemplate)
                .nodeInfoProvider(mock(NodeInfoProvider.class))
                .eventConsumer(mock(MessageBus.class))
                .objectMapper(new ObjectMapper())
                .build();
    }

    @Test
    public void testGetContainers() {
        List<DockerContainer> containers = service.getContainers(new GetContainersArg(true));
        containers.forEach(System.out::println);

        for (DockerContainer container: containers) {
            ContainerDetails cds = service.getContainer(container.getId());
            System.out.println(cds);
            ImageDescriptor image = service.pullImage(cds.getConfig().getImage(), System.out::println);
            System.out.println(image);
        }
    }

    @Test
    public void testStartAndCreateContainer() {
        List<DockerContainer> containers = service.getContainers(new GetContainersArg(true));
        Optional<String> cid = containers.stream()
                .filter(c -> "cont1".equals(c.getName()))
                .findFirst().map(DockerContainer::getId);
        String id = cid.orElseGet(() -> {
            CreateContainerCmd cmd = new CreateContainerCmd();
            cmd.setImage("hello-world");
            cmd.setName("cont1");
            CreateContainerResponse res = service.createContainer(cmd);
            return res.getId();
        });

        service.startContainer(id);
    }

    @Test
    public void testStopContainer() {
       Optional.ofNullable(service.getContainer("dev1")).ifPresent(c -> {
           StopContainerArg arg =
                   StopContainerArg.builder().id(c.getId()).build();
           System.out.println(service.stopContainer(arg));
       });
    }

    @Test
    public void testTag() {
        HttpAuthInterceptor.setCurrentName("test.com");
        TagImageArg arg = TagImageArg.builder().remote(true)
                .imageName("cluster-manager")
                .repository("test.com")
                .currentTag("latest")
                .newTag("testTag").build();

        ServiceCallResult tag = service.createTag(arg);
        assertNotNull(tag);
    }

    @Test
    public void testGetSwarm() {
        SwarmInspectResponse swarm = service.getSwarm();
        System.out.println(swarm);
        assertNotNull(swarm);
    }

    @Test
    public void testGetServices() {
        List<Service> services = service.getServices(new GetServicesArg());
        System.out.println(services);
        assertNotNull(services);
    }

    @Test
    public void testGetTasks() {
        List<Task> tasks = service.getTasks(new GetTasksArg());
        System.out.println(tasks);
        assertNotNull(tasks);
    }

    @Test
    public void testGetVolumes() {
        List<Volume> volumes = service.getVolumes(new GetVolumesArg());
        System.out.println(volumes);
        assertNotNull(volumes);
    }
}