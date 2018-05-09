package edu.scut.cs.hm.model.container;

import edu.scut.cs.hm.docker.model.image.ImageDescriptor;
import edu.scut.cs.hm.model.source.ContainerSource;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Context of config parser
 */
@Data
@Builder
public class ContainerCreationContext {
    @NotNull
    private ImageDescriptor image;
    @NotNull
    private String imageName;
    private String cluster;

    private final List<ContainerSource> argList = new ArrayList<>();

    public void addCreateContainerArg(ContainerSource createContainerArg) {
        argList.add(createContainerArg);
    }

}
