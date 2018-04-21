package edu.scut.cs.hm.admin.security;

import edu.scut.cs.hm.common.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

@Slf4j
public final class TempAuth implements AutoCloseable {

    private final Authentication newAuth;
    private final SecurityContext context;
    private final SecurityContext oldContext;

    private AccessContextHolder aclHolder;

    private TempAuth(Authentication newAuth) {
        Assert.notNull(newAuth, "Authentication is null");
        this.newAuth = newAuth;
        this.oldContext = SecurityContextHolder.getContext();
        this.context = SecurityContextHolder.createEmptyContext();
    }

    /**
     * Do <code>open(SecurityUtils.AUTH_SYSTEM)</code>
     * @return
     */
    public static TempAuth asSystem() {
        return open(SecurityUtils.AUTH_SYSTEM);
    }

    public static TempAuth open(Authentication auth) {
        TempAuth tempAuth = new TempAuth(auth);
        tempAuth.init();
        return tempAuth;
    }

    // open a new context for new authentication
    private void init() {
        context.setAuthentication(newAuth);
        SecurityContextHolder.setContext(context);
        AccessContextFactory acf = AccessContextFactory.getInstanceOrNull();
        if (acf != null) {
            // we open a new context
            aclHolder = acf.open();
        }
    }

    /**
     * Close temp opened AccessContext
     */
    @Override
    public void close() {
        SecurityContext currContext = SecurityContextHolder.getContext();
        if(currContext != context) {
            log.warn("Current context \"{}\" not equal with expected: \"{}\"", currContext, context);
        }
        SecurityContextHolder.setContext(oldContext);
        if(aclHolder != null) {
            aclHolder.close();
        }
    }
}
