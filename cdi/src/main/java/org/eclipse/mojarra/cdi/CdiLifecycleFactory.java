package org.eclipse.mojarra.cdi;

import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * The CDI LifecycleFactory.
 */
public class CdiLifecycleFactory extends LifecycleFactory {

    /**
     * Constructor.
     *
     * @param wrapped the wrapped lifecycle factory.
     */
    public CdiLifecycleFactory(LifecycleFactory wrapped) {
        super(wrapped);
    }

    @Override
    public void addLifecycle(String lifecycleId, Lifecycle lifecycle) {
        // because we are using CDI to manage our lifecycles this is a no-op.
    }

    @Override
    public Lifecycle getLifecycle(String lifecycleId) {
        Lifecycle result = null;
        if (lifecycleId.equals(LifecycleFactory.DEFAULT_LIFECYCLE)) {
            result = getWrapped().getLifecycle(lifecycleId);
        } else {
            BeanManager beanManager = getBeanManager();
            AnnotatedType<Lifecycle> type = beanManager.createAnnotatedType(Lifecycle.class);
            Set<Bean<?>> beans = beanManager.getBeans(type.getBaseType(), NamedLiteral.of(lifecycleId));
            Iterator<Bean<?>> iterator = beans.iterator();
            while (iterator.hasNext()) {
                Bean<?> bean = iterator.next();
                Named named = bean.getBeanClass().getAnnotation(Named.class);
                if (named.value().equals(lifecycleId)) {
                    result = (Lifecycle) CDI.current().select(named).get();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Get the bean manager, either from 'java:comp/BeanManager' or
     * 'java:comp/env/BeanManager'.
     *
     * @return the bean manager.
     */
    public BeanManager getBeanManager() {
        BeanManager beanManager = null;
        try {
            InitialContext initialContext = new InitialContext();
            beanManager = (BeanManager) initialContext.lookup("java:comp/BeanManager");
        } catch (NamingException ne) {
        }
        if (beanManager == null) {
            try {
                InitialContext initialContext = new InitialContext();
                beanManager = (BeanManager) initialContext.lookup("java:comp/env/BeanManager");
            } catch (NamingException ne) {
            }
        }
        return beanManager;
    }

    @Override
    public Iterator<String> getLifecycleIds() {
        ArrayList<String> lifecycleIds = new ArrayList<>();
        getWrapped().getLifecycleIds().forEachRemaining(lifecycleIds::add);
        BeanManager beanManager = getBeanManager();
        AnnotatedType<Lifecycle> type = beanManager.createAnnotatedType(Lifecycle.class);
        Set<Bean<?>> beans = beanManager.getBeans(type.getBaseType());
        Iterator<Bean<?>> iterator = beans.iterator();
        while (iterator.hasNext()) {
            Bean<?> bean = iterator.next();
            if (bean.getBeanClass().isAnnotationPresent(Named.class)) {
                Named named = bean.getBeanClass().getAnnotation(Named.class);
                lifecycleIds.add(named.value());
            }
        }
        return lifecycleIds.iterator();
    }
}
