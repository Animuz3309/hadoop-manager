package edu.scut.cs.hm.model.filter;

import edu.scut.cs.hm.admin.component.FilterFactory;

import java.util.function.Predicate;

/**
 * Function for filtration.
 */
@FunctionalInterface
public interface Filter extends Predicate<Object> {

    static Filter any() {
        return  new Filter() {
            @Override
            public boolean test(Object o) {
                return true;
            }

            @Override
            public String getExpression() {
                return FilterFactory.ANY;
            }
        };
    }

    static Filter noOne() {
        return new Filter() {
            @Override
            public boolean test(Object o) {
                return false;
            }

            @Override
            public String getExpression() {
                return FilterFactory.NO_ONE;
            }
        };
    }

    default String getExpression() {
        return null;
    }
}
