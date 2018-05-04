package edu.scut.cs.hm.model.container;

import edu.scut.cs.hm.docker.DockerService;
import org.springframework.util.Assert;

import java.util.function.Supplier;

public abstract class AbstractContainersManager implements ContainersManager {
    protected final Supplier<DockerService> supplier;

    public AbstractContainersManager(Supplier<DockerService> supplier) {
        this.supplier = supplier;
    }

    protected DockerService getDocker() {
        DockerService service = supplier.get();
        Assert.notNull(service, "supplier " + supplier + " return null value");
        return service;
    }
}
