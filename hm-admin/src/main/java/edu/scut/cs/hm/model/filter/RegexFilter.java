package edu.scut.cs.hm.model.filter;

import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexFilter extends AbstractTextFilter {

    public static final String PROTO = "regex";
    private final Pattern namePattern;
    private final String expr;

    public RegexFilter(String pattern) {
        Assert.notNull(pattern, "pattern must not be null");
        this.expr = PROTO + ":" + pattern;
        this.namePattern = Pattern.compile(pattern);
    }

    @Override
    public String getExpression() {
        return expr;
    }

    @Override
    protected boolean innerTest(CharSequence text) {
        Matcher matcher = namePattern.matcher(text);
        return matcher.matches();
    }

}