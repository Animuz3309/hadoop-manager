package edu.scut.cs.hm.admin.service;

import com.google.common.base.Predicate;
import edu.scut.cs.hm.admin.web.model.UiSearchQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.MoreObjects.firstNonNull;
import static edu.scut.cs.hm.admin.web.model.UiSearchQuery.SortOrder.ASC;
import static edu.scut.cs.hm.common.utils.PojoBeanUtils.getValue;

@Slf4j
@Service
public class FilterService {
    private static final SpelExpressionParser parser = new SpelExpressionParser();

    public <T> Collection<T> listNodes(Collection<T> collection, UiSearchQuery searchQuery) {
        final String where = searchQuery.getWheres();
        final List<UiSearchQuery.SearchOrder> orders = firstNonNull(searchQuery.getOrders(), Collections.emptyList());
        //init sorting
        Comparator<T> comparing = (t1, t2) -> 0;
        for (UiSearchQuery.SearchOrder order: orders) {
            comparing = comparing.thenComparing((nodeInfo1, nodeInfo2) -> {
                Object value1 = getValue(nodeInfo1, order.getField());
                Object value2 = getValue(nodeInfo2, order.getField());

                Comparable v1 = value1 instanceof Comparable ? (Comparable) value1 : null;
                Comparable v2 = value2 instanceof Comparable ? (Comparable) value2 : null;
                @SuppressWarnings("unchecked") int result = ObjectUtils.compare(v1, v2);
                return order.getOrder() == ASC ? result : -result;
            });
        }

        final List<Where<T>> wheres = new ArrayList<>();
        if (StringUtils.hasText(where)) {
            wheres.add(fromPredicate(w -> {
                try {
                    SpelExpression expr = parser.parseRaw(where);
                    return (Boolean) expr.getValue(w);
                } catch (Exception e) {
                    log.error("error during parsing '" + where + "', '" + w + "'", e);
                    return false;
                }
            }));
        }

        wheres.add(topN(comparing, searchQuery.getPage() * searchQuery.getSize(), searchQuery.getSize()));

        final Where<T> compositeWhere = wheres.stream().reduce(c -> c, (c1, c2) -> (s -> c2.apply(c1.apply(s))));

        return compositeWhere.apply(collection.stream()).collect(Collectors.toList());
    }

    @FunctionalInterface
    private interface Where<T> {
        Stream<T> apply(Stream<T> s);
    }

    private <T> Where<T> topN(Comparator<T> cmp, long from, long size) {
        return stream -> stream.sorted(cmp).skip(from).limit(size);
    }

    private <T> Where<T> fromPredicate(Predicate<T> pred) {
        return stream -> stream.filter(pred);
    }


}
