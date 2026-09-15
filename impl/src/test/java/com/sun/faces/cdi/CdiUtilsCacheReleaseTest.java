/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.faces.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.faces.component.behavior.Behavior;

import com.sun.faces.mock.MockBeanManager;

import org.junit.jupiter.api.Test;

/**
 * Covers how {@link CdiUtils} releases an undeployed application -- the hot-deployment behaviour that
 * commit {@code c51a77fa1} ("Fix hotdeployment leak discovered in 4.1 TCK, #5752") introduced, and
 * that the move to a registration-based cache has to preserve.
 *
 * <p>The caches hold a {@link BeanManager} and its resolved {@link Bean}s strongly, and through them
 * the application's class loader. Two rules keep that bounded, and these tests pin both down:
 *
 * <ul>
 * <li>Only {@link CdiUtils#registerBeanManager} can add an application, so nothing a request thread
 *     does can (re)introduce one -- see
 *     {@link #straggerCallAfterUndeployment_cannotResurrectTheEntry}.</li>
 * <li>{@link CdiUtils#unregisterBeanManager} removes exactly one application and releases it, so
 *     undeploying one application does not disturb the others -- see
 *     {@link #unregister_leavesOtherApplicationsUntouched}.</li>
 * </ul>
 */
public class CdiUtilsCacheReleaseTest {

    private static final int GC_ATTEMPTS = 50;

    /**
     * A registered application is held strongly for as long as it is registered -- it has to be, the
     * cache is keyed by it -- and unregistering is what lets it go. This is the leak of issue #5752 and
     * its cure, now expressed as the pair of operations rather than as a weak reference that only
     * sometimes fires.
     */
    @Test
    public void unregister_releasesTheApplicationForGarbageCollection() throws Exception {
        BeanManager beanManager = new BackReferencingBeanManager();
        CdiUtils.registerBeanManager(beanManager);

        Bean<?> resolved = CdiUtils.resolveBean(beanManager, Behavior.class);
        assertNotNull(resolved, "the stub BeanManager resolves a bean, so a positive entry is cached");
        assertSame(beanManager, ((BackReferencingBean) resolved).owningBeanManager,
                "the cached Bean references its own BeanManager, exactly as Weld's Bean implementations do");

        WeakReference<BeanManager> undeployed = new WeakReference<>(beanManager);
        resolved = null;
        beanManager = null; // Undeployment: the container drops the application's BeanManager.

        assertFalse(awaitCleared(undeployed),
                "while registered, the application is reachable from the cache and cannot be collected");

        CdiUtils.unregisterBeanManager(undeployed.get()); // == CdiExtension.beforeShutdown

        assertTrue(awaitCleared(undeployed),
                "unregistering releases the undeployed application's BeanManager, its beans and its class loader");
    }

    /**
     * Undeploying one application must not cost the others their warm caches. The previous
     * implementation could only clear wholesale, because it could not match a defunct deployment's key;
     * registration gives each application an entry that can be removed on its own.
     */
    @Test
    public void unregister_leavesOtherApplicationsUntouched() {
        ResolvesNothingBeanManager applicationOne = new ResolvesNothingBeanManager();
        ResolvesNothingBeanManager applicationTwo = new ResolvesNothingBeanManager();

        CdiUtils.createBehavior(applicationOne, "jakarta.faces.Ajax");
        CdiUtils.createBehavior(applicationTwo, "jakarta.faces.Ajax");
        CdiUtils.createBehavior(applicationOne, "jakarta.faces.Ajax");
        CdiUtils.createBehavior(applicationTwo, "jakarta.faces.Ajax");
        assertEquals(1, applicationOne.lookups.get(), "warm");
        assertEquals(1, applicationTwo.lookups.get(), "warm");

        CdiUtils.unregisterBeanManager(applicationOne); // application one shuts down

        CdiUtils.createBehavior(applicationTwo, "jakarta.faces.Ajax");
        assertEquals(1, applicationTwo.lookups.get(),
                "the surviving application keeps its warm cache; only the undeployed one was dropped");

        CdiUtils.createBehavior(applicationOne, "jakarta.faces.Ajax");
        assertEquals(2, applicationOne.lookups.get(), "the undeployed application is no longer cached");
    }

    /**
     * A request still draining while the application shuts down keeps working, and cannot put the
     * application back into the cache. Without this the targeted removal would be pointless: the
     * {@code FacesContext} release path alone calls into {@link CdiUtils} on every request.
     */
    @Test
    public void straggerCallAfterUndeployment_cannotResurrectTheEntry() throws Exception {
        ResolvesNothingBeanManager application = new ResolvesNothingBeanManager();
        CdiUtils.createBehavior(application, "jakarta.faces.Ajax");

        CdiUtils.unregisterBeanManager(application);

        assertNull(CdiUtils.createBehavior(application, "jakarta.faces.Ajax"), "the straggler still resolves");
        assertNull(CdiUtils.resolveFacesContextProducerBean(application), "so does the FacesContext release path");

        assertFalse(caches().containsKey(application),
                "a call after undeployment must not re-add the application to the cache");
    }

    /**
     * Registering again starts from an empty cache. This is what protects a redeployment on an
     * implementation whose {@code BeanManager} equality is derived from the bean archive id (Weld's is):
     * the new deployment produces a key equal to the old one, and inheriting its entry would mean
     * resolving against beans whose context is gone.
     */
    @Test
    public void register_discardsWhatWasCachedForAnEqualKey() {
        ResolvesNothingBeanManager application = new ResolvesNothingBeanManager();

        CdiUtils.createBehavior(application, "jakarta.faces.Ajax");
        CdiUtils.createBehavior(application, "jakarta.faces.Ajax");
        assertEquals(1, application.lookups.get(), "warm");

        CdiUtils.registerBeanManager(application); // redeployment onto an equal key

        CdiUtils.createBehavior(application, "jakarta.faces.Ajax");
        assertEquals(2, application.lookups.get(), "the redeployed application starts with an empty cache");
    }

    /**
     * Both {@code CdiExtension.beforeShutdown} and {@code ConfigureListener.contextDestroyed} unregister
     * the same application, and either may run first, so a second removal must be a no-op rather than an
     * error. Unregistering something that was never registered must be harmless too.
     */
    @Test
    public void unregister_isIdempotentAndSafeForUnknownBeanManagers() {
        ResolvesNothingBeanManager application = new ResolvesNothingBeanManager();
        CdiUtils.createBehavior(application, "jakarta.faces.Ajax");

        CdiUtils.unregisterBeanManager(application);
        CdiUtils.unregisterBeanManager(application);
        CdiUtils.unregisterBeanManager(new MockBeanManager());
        CdiUtils.unregisterBeanManager(null);

        assertNull(CdiUtils.createBehavior(application, "jakarta.faces.Ajax"), "still resolves, just uncached");
    }

    // --- helpers ---------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Map<BeanManager, ?> caches() throws Exception {
        Field field = CdiUtils.class.getDeclaredField("CACHES");
        field.setAccessible(true);
        return (Map<BeanManager, ?>) field.get(null);
    }

    /**
     * Runs GC repeatedly and reports whether {@code reference} was cleared. A {@code false} result is
     * conclusive for a strongly reachable referent (it can never be cleared); a {@code true} result
     * means the referent was unreachable.
     */
    private static boolean awaitCleared(WeakReference<?> reference) throws InterruptedException {
        for (int attempt = 0; attempt < GC_ATTEMPTS; attempt++) {
            if (reference.get() == null) {
                return true;
            }
            System.gc();
            Thread.sleep(10);
        }
        return reference.get() == null;
    }

    /**
     * A {@link BeanManager} that resolves a {@link Bean} holding a reference back to itself, which is how
     * Weld's bean implementations behave ({@code ManagedBean} holds its {@code BeanManagerImpl}).
     */
    private static final class BackReferencingBeanManager extends MockBeanManager {

        private final Bean<?> bean = new BackReferencingBean(this);

        @Override
        public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
            return Collections.singleton(bean);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
            return (Bean<? extends X>) bean;
        }
    }

    private static final class BackReferencingBean implements Bean<Object> {

        final BeanManager owningBeanManager;

        BackReferencingBean(BeanManager owningBeanManager) {
            this.owningBeanManager = owningBeanManager;
        }

        @Override public Set<Type> getTypes() { return Collections.singleton(Behavior.class); }
        @Override public Set<Annotation> getQualifiers() { return Collections.emptySet(); }
        @Override public Class<? extends Annotation> getScope() { return Dependent.class; }
        @Override public String getName() { return null; }
        @Override public Set<Class<? extends Annotation>> getStereotypes() { return Collections.emptySet(); }
        @Override public Class<?> getBeanClass() { return BackReferencingBean.class; }
        @Override public boolean isAlternative() { return false; }
        @Override public Object create(CreationalContext<Object> ctx) { return null; }
        @Override public void destroy(Object instance, CreationalContext<Object> ctx) { }
        @Override public Set<InjectionPoint> getInjectionPoints() { return Collections.emptySet(); }
    }

    /**
     * A registered {@link BeanManager} that resolves nothing, so only negative entries are cached, and
     * counts how often it was consulted.
     */
    private static final class ResolvesNothingBeanManager extends MockBeanManager {

        final AtomicInteger lookups = new AtomicInteger();

        ResolvesNothingBeanManager() {
            CdiUtils.registerBeanManager(this);
        }

        @Override
        public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
            lookups.incrementAndGet();
            return Collections.emptySet();
        }

        @Override
        public Set<Bean<?>> getBeans(String name) {
            lookups.incrementAndGet();
            return Collections.emptySet();
        }

        @Override
        public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
            return null;
        }

        @Override
        public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
            return null;
        }
    }
}
