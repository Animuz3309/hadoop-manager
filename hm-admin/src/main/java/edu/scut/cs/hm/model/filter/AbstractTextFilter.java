package edu.scut.cs.hm.model.filter;

import edu.scut.cs.hm.model.Named;

public abstract class AbstractTextFilter implements Filter {
    @Override
    public boolean test(Object o) {
        CharSequence text;
        if(o == null) {
            text = null;
        } else if(o instanceof Named) {
            text = ((Named)o).getName();
        } else if(o instanceof CharSequence) {
            text = (CharSequence) o;
        } else {
            text = o.toString();
        }
        return innerTest(text);
    }

    protected abstract boolean innerTest(CharSequence text);
}
