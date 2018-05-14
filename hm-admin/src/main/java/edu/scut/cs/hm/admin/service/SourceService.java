package edu.scut.cs.hm.admin.service;

import edu.scut.cs.hm.admin.component.ContainerSourceFactory;
import edu.scut.cs.hm.model.cluster.DiscoveryStorage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class SourceService {

    private final DiscoveryStorage discoveryStorage;
    private final ContainerSourceFactory containerSourceFactory;
}
