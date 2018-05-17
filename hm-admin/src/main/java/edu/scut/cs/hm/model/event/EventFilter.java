package edu.scut.cs.hm.model.event;

import edu.scut.cs.hm.admin.component.FilterFactory;
import edu.scut.cs.hm.admin.web.model.msg.UiCountResult;
import edu.scut.cs.hm.model.filter.Filter;

public class EventFilter {
    private int counter = 0;
    private final String expr;
    private final Filter filter;

    public EventFilter(FilterFactory ff, String expr) {
        this.expr = expr;
        this.filter = ff.createFilter(expr);
    }

    public void collect(Object o) {
        if(filter.test(o)) {
            counter++;
        }
    }

    int getResult() {
        return counter;
    }

    @Override
    public String toString() {
        return "FilterCollector{" +
                "expr='" + expr + '\'' +
                ", counter=" + counter +
                '}';
    }

    public UiCountResult.FilteredResult toUi() {
        UiCountResult.FilteredResult ui = new UiCountResult.FilteredResult();
        ui.setCount(counter);
        ui.setFilter(expr);
        return ui;
    }
}
