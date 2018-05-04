package edu.scut.cs.hm.model.filter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractFilter<T> implements Filter {

    @SuppressWarnings("unchecked")
    public boolean test(Object o) {
        if (o == null) {
            return false;
        }
        try {
            T data = (T) o;
            return innerTest(data);
        } catch (ClassCastException ignored) {
            log.debug("filter {} can't be applied to {}", this, o);
            return true;
        } catch (Exception e) {
            log.error("Can't apply filter", e);
            return false;
        }
    }

    protected abstract boolean innerTest(T o);
}
