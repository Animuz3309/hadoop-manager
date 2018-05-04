package edu.scut.cs.hm.admin.component;

import edu.scut.cs.hm.common.utils.StringUtils;
import edu.scut.cs.hm.model.filter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
/**
 * Filter factory
 */
public class FilterFactory {
    public final static String ANY = "any:*";
    public final static String NO_ONE = "noOne:*";

    public interface Factory {
        Filter create(String expr);
        default String getProtocol() {
            return null;
        }
    }

    private final ConcurrentMap<String, Factory> factories = new ConcurrentHashMap<>();

    public FilterFactory() {
        registerFilter(expr -> Filter.any(), "any");
        registerFilter(expr -> Filter.noOne(), "noOne");
        // todo register image, label an so on filter
        registerFilter(ListFilter::new, ListFilter.PROTO);
        registerFilter(RegexFilter::new, RegexFilter.PROTO);
        registerFilter(PatternFilter::new, PatternFilter.PROTO);
        registerFilter(ClusterFilter::new, ClusterFilter.PROTO);
    }

    @Autowired(required = false)
    public void onFilterFactories(List<Factory> factories) {
        if(factories == null) {
            return;
        }
        factories.forEach(this::registerFilter);
    }

    public void registerFilter(Factory factory) {
        String protocol = factory.getProtocol();
        Assert.notNull(protocol, factory + " got invalid protocol.");
        registerFilter(factory, protocol);
    }

    public void registerFilter(Factory factory, String protocol) {
        factories.put(protocol, factory);
    }

    public Filter createFilter(String expr) {
        String proto = StringUtils.before(expr, ':');
        Factory ff = factories.get(proto);
        Assert.notNull(ff, "can not find factory for: " + expr);
        return ff.create(expr.substring(proto.length() + 1));
    }
}
