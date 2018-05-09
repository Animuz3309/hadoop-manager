package edu.scut.cs.hm.admin.component;

import edu.scut.cs.hm.docker.model.container.ContainerConfig;
import edu.scut.cs.hm.docker.model.image.ImageDescriptor;
import edu.scut.cs.hm.model.container.ConfigsFetcher;
import edu.scut.cs.hm.model.container.ContainerCreationContext;
import edu.scut.cs.hm.model.container.Parser;
import edu.scut.cs.hm.model.source.ContainerSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fetches settings from Image
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigsFetcherImage implements ConfigsFetcher {

    public static final String IMAGE_ARGS = "arg.";

    private final List<Parser> parser;

    @Override
    public void resolveProperties(ContainerCreationContext context) {
        ImageDescriptor image = context.getImage();
        if (image == null) {
            // it look like error, but anyway must be reported not here
            return;
        }
        ContainerConfig containerConfig = image.getContainerConfig();
        if (containerConfig == null) {
            return;
        }
        log.info("parsing image labels: {}", image.getId());
        Map<String, String> labels = containerConfig.getLabels();
        if (!CollectionUtils.isEmpty(labels)) {
            Map<String, Object> parsedLabels = new HashMap<>();
            for (Map.Entry<String, String> entry : labels.entrySet()) {
                //replacing LABEL arg.ports=8761:8761 -> ports=8761:8761
                parsedLabels.put(entry.getKey().replace(IMAGE_ARGS, ""), entry.getValue());
            }
            parser.forEach(a -> a.parse(parsedLabels, context));

        }
        ContainerSource nc = new ContainerSource();
        nc.getLabels().putAll(containerConfig.getLabels());
        context.addCreateContainerArg(nc);
    }

}
