package edu.scut.cs.hm.admin.component;

import edu.scut.cs.hm.common.utils.PojoUtils;
import edu.scut.cs.hm.docker.model.image.ImageDescriptor;
import edu.scut.cs.hm.common.pojo.Property;
import edu.scut.cs.hm.model.container.ConfigProvider;
import edu.scut.cs.hm.model.container.ConfigsFetcher;
import edu.scut.cs.hm.model.container.ContainerCreationContext;
import edu.scut.cs.hm.model.source.ContainerSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Resolves properties from multiple sources and merge to one CreateContainerArg
 */
@Slf4j
@Component
public class ConfigProviderImpl implements ConfigProvider {

    private final List<ConfigsFetcher> fetcherList;

    @Autowired
    public ConfigProviderImpl(List<ConfigsFetcher> fetchers) {
        fetcherList = fetchers;
        fetcherList.sort(AnnotationAwareOrderComparator.INSTANCE);
    }

    public ContainerSource resolveProperties(String cluster, ImageDescriptor image, String imageName, ContainerSource original) {

        ContainerCreationContext context = ContainerCreationContext.builder().cluster(cluster).image(image)
                .imageName(imageName).build();
        for (ConfigsFetcher configsFetcher : fetcherList) {
            try {
                configsFetcher.resolveProperties(context);
            } catch (Exception e) {
                log.error("can't process config for image " + image, e);
            }
        }
        List<ContainerSource> configs = context.getArgList();
        configs.add(original);
        ContainerSource result = new ContainerSource();
        Map<String, Property> load = PojoUtils.load(ContainerSource.class);
        for (ContainerSource srcConfig : configs) {
            forConfig(result, load, srcConfig);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void forConfig(ContainerSource dest, Map<String, Property> props, ContainerSource srcConfig) {
        for (Property prop : props.values()) {
            try {
                Object o = prop.get(srcConfig);
                if (o == null) {
                    continue;
                }
                if (prop.isWritable()) {
                    Class<?> type = prop.getType();
                    if(Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
                        // we must not set collections, just add elements to them
                        log.warn("Ignore modifiable property of collection type: {}.{}", prop.getDeclaringClass(), prop.getName());
                        continue;
                    }
                }
                // we try to accumulate value for non null collection
                // note that set collections is bad way because it may be shared between objects and it
                // may cause difficult localised errors
                if (o instanceof Collection) {
                    Object r = prop.get(dest);
                    if (r != null && r instanceof Collection) {
                        Collection<Object> destCol = (Collection<Object>) r;
                        Collection<Object> coll = new LinkedHashSet<>((Collection<Object>) o);
                        coll.removeAll(destCol);
                        destCol.addAll(coll);
                        continue;
                    }
                }
                if (o instanceof Map) {
                    Object r = prop.get(dest);
                    if (r != null && r instanceof Map) {
                        Map<Object, Object> destMap = (Map<Object, Object>) r;
                        destMap.putAll((Map<Object, Object>) o);
                        continue;
                    }
                }
                if (prop.isWritable()) {
                    prop.set(dest, o);
                }
            } catch (Exception e) {
                log.error("Can't process property: " + prop.getName(), e);
            }

        }
    }


}
