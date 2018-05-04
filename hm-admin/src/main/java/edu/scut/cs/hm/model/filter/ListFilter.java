package edu.scut.cs.hm.model.filter;

import com.google.common.base.MoreObjects;
import edu.scut.cs.hm.model.Named;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

public class ListFilter extends AbstractFilter<Named> {

    public static final String PROTO = "list";
    private final List<String> names;
    private final String expr;

    public ListFilter(String listNames) {
        this.expr = PROTO + ":" + listNames;
        Assert.notNull(listNames, "NamePattern must not be null");
        names = Arrays.asList(listNames.split(","));
    }

    @Override
    public String getExpression() {
        return expr;
    }

    @Override
    protected boolean innerTest(Named ifc) {
        return names.contains(ifc.getName());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("names", names)
                .add("expr", expr)
                .toString();
    }
}