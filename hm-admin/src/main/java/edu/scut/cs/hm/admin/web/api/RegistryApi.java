package edu.scut.cs.hm.admin.web.api;

import edu.scut.cs.hm.admin.service.RegistryFactory;
import edu.scut.cs.hm.common.security.Authorities;
import edu.scut.cs.hm.model.registry.RegistryConfig;
import edu.scut.cs.hm.model.registry.RegistryRepository;
import edu.scut.cs.hm.model.registry.core.RegistryService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping(value = "/api/registries", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RegistryApi {

    private final RegistryRepository registryRepository;
    private final RegistryFactory registryFactory;

    @RequestMapping(value = "", method = GET)
    @ApiOperation("list of available registries")
    public Collection<RegistryConfig> availableRegistries() {
        Collection<String> availableRegistries = registryRepository.getAvailableRegistries();
        return availableRegistries.stream().map(registryRepository::getByName).map(this::map).collect(Collectors.toList());
    }

    private RegistryConfig map(RegistryService registry) {
        // see that we explicitly clone config
        RegistryConfig config = registry.getConfig().clone();
        config.cleanCredentials();
        String title = config.getTitle();
        if(title == null) {
            config.setTitle(config.getName());
        }
        return config;
    }

    @Secured(Authorities.ADMIN_ROLE)
    @RequestMapping(value = "", method = {PUT, POST})
    public RegistryConfig addRegistry(@RequestBody @Validated RegistryConfig config) {
        final String oldName = config.getName();
        if(oldName != null) {
            registryRepository.unRegister(oldName);
        }
        config.setName(null);//name must been calculated from other config attributes
        registryFactory.complete(config);
        RegistryService registryService = registryFactory.createRegistryService(config);
        registryRepository.register(registryService);
        return map(registryService);
    }

    @Secured(Authorities.ADMIN_ROLE)
    @RequestMapping(value = "", method = DELETE)
    public void deleteRegistry(@RequestParam(value = "name") String name) {
        registryRepository.unRegister(name);
    }

    @RequestMapping(value = "/refresh", method = PUT)
    @ApiOperation("Refresh registry")
    public RegistryConfig refreshRegistry(@RequestParam(value = "name") String name) {
        RegistryService registry = registryRepository.getByName(name);
        registry.checkHealth();
        return map(registry);
    }
}
