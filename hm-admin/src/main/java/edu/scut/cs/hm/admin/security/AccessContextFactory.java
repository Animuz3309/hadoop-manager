package edu.scut.cs.hm.admin.security;

import edu.scut.cs.hm.common.security.acl.ExtPermissionGrantingStrategy;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Factory of {@link AccessContext}
 */
public class AccessContextFactory {
    private static final ThreadLocal<AccessContext> TL = new ThreadLocal<>();
    private static final Object lock = new Object();
    private static volatile AccessContextFactory instance;

    final AclService aclService;
    final ExtPermissionGrantingStrategy pgs;
    final SidRetrievalStrategy sidStrategy;

    public AccessContextFactory(AclService aclService, ExtPermissionGrantingStrategy pgs, SidRetrievalStrategy sidStrategy) {
        this.aclService = aclService;
        this.pgs = pgs;
        this.sidStrategy = sidStrategy;
    }

    /**
     * It must not be public
     * @return current factory
     */
    static AccessContextFactory getInstance() {
        AccessContextFactory acf = getInstanceOrNull();
        if(acf == null) {
            throw new IllegalStateException("No instance.");
        }
        return acf;
    }

    static AccessContextFactory getInstanceOrNull() {
        synchronized (lock) {
            return instance;
        }
    }

    @PostConstruct
    public void postConstruct() {
        synchronized (lock) {
            if(instance != null) {
                throw new IllegalStateException("Factory already has instance.");
            }
            instance = this;
        }
    }

    @PreDestroy
    public void preDestroy() {
        synchronized (lock) {
            if(instance != this) {
                throw new IllegalStateException("Factory has different instance.");
            }
            instance = null;
        }
    }

    /**
     * Obtain context from thread local. <p/>
     * Throw exception when existed local context not complies with current authentication.
     * @return
     */
    public static AccessContext getLocalContext() {
        AccessContext ac = TL.get();
        if (ac != null) {
            ac.assertActual();  // if not actual throw exception
        } else {
            throw new IllegalStateException("No local context");
        }
        return ac;
    }

    /**
     *  Obtain context from thread local, if it actual and not null,
     *  otherwise create new context (but not place it to thread local).
     * @return
     */
    public AccessContext getContext() {
        AccessContext ac = TL.get();
        // ac == null 且 此线程ac保存的authentication 不是当前请求的
        if (ac == null || !ac.isActual()) {
            ac = new AccessContext(this);
        }
        return ac;
    }

    /**
     * Open context and place it to thread local.
     * @see #getContext()
     * @return
     */
    public AccessContextHolder open() {
        AccessContext old = TL.get();
        if (old != null && old.isActual()) {
            return new AccessContextHolder(old, () -> {});
        }
        AccessContext ac = new AccessContext(this);
        TL.set(ac);
        return new AccessContextHolder(ac, () -> {
            // 关闭打开的新的context
            AccessContext curr = TL.get();
            // 必须判断当前threadlocal里的ac 就是之前创建的ac
            Assert.isTrue(ac == curr, "Invalid current context: " + curr + " expect: " + ac);
            TL.set(old);
        });
    }

}
