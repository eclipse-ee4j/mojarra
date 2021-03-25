package org.eclipse.mojarra.cdi;

import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * The CDI RenderKitFactory.
 */
public class CdiRenderKitFactory extends RenderKitFactory {

    /**
     * Stores the BeanManager.
     */
    public BeanManager beanManager;

    /**
     * Constructor.
     */
    public CdiRenderKitFactory() {
    }

    /**
     * Constructor.
     *
     * @param wrapped the wrapped RenderKitFactory.
     */
    public CdiRenderKitFactory(RenderKitFactory wrapped) {
        super(wrapped);
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
    }

    @Override
    public void addRenderKit(String renderKitId, RenderKit renderKit) {
        // because we are using CDI to manage our render-kits this is a no-op.
    }

    @Override
    public RenderKit getRenderKit(FacesContext facesContext, String renderKitId) {
        RenderKit result = null;
        if (renderKitId.equals(RenderKitFactory.HTML_BASIC_RENDER_KIT)) {
            result = getWrapped().getRenderKit(facesContext, renderKitId);
        } else {
            AnnotatedType<RenderKit> type = beanManager.createAnnotatedType(RenderKit.class);
            Set<Bean<?>> beans = beanManager.getBeans(type.getBaseType(), NamedLiteral.of(renderKitId));
            Iterator<Bean<?>> iterator = beans.iterator();
            while (iterator.hasNext()) {
                Bean<?> bean = iterator.next();
                Named named = bean.getBeanClass().getAnnotation(Named.class);
                if (named.value().equals(renderKitId)) {
                    result = (RenderKit) CDI.current().select(named).get();
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Iterator<String> getRenderKitIds() {
        ArrayList<String> renderKitIds = new ArrayList<>();
        getWrapped().getRenderKitIds().forEachRemaining(renderKitIds::add);
        AnnotatedType<RenderKit> type = beanManager.createAnnotatedType(RenderKit.class);
        Set<Bean<?>> beans = beanManager.getBeans(type.getBaseType());
        Iterator<Bean<?>> iterator = beans.iterator();
        while (iterator.hasNext()) {
            Bean<?> bean = iterator.next();
            if (bean.getBeanClass().isAnnotationPresent(Named.class)) {
                Named named = bean.getBeanClass().getAnnotation(Named.class);
                renderKitIds.add(named.value());
            }
        }
        return renderKitIds.iterator();
    }
}
