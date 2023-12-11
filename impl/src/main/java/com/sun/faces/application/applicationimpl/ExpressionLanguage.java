/*
 * Copyright (c) 2023 Contributors to Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.application.applicationimpl;

import static com.sun.faces.util.MessageUtils.ILLEGAL_ATTEMPT_SETTING_APPLICATION_ARTIFACT_ID;
import static com.sun.faces.util.MessageUtils.getExceptionMessageString;
import static com.sun.faces.util.Util.getCdiBeanManager;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.el.FacesCompositeELResolver;
import jakarta.el.CompositeELResolver;
import jakarta.el.ELContextListener;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.inject.spi.el.ELAwareBeanManager;
import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExpressionLanguage {

    private static final ELContextListener[] EMPTY_EL_CTX_LIST_ARRAY = {};

    private final ApplicationAssociate associate;

    private final List<ELContextListener> elContextListeners;
    private final CompositeELResolver elResolvers;
    private volatile FacesCompositeELResolver compositeELResolver;

    public ExpressionLanguage(ApplicationAssociate applicationAssociate) {
        associate = applicationAssociate;
        elContextListeners = new CopyOnWriteArrayList<>();
        elResolvers = new CompositeELResolver();
    }

    /*
     * @see jakarta.faces.application.Application#addELContextListener(jakarta.el.ELContextListener)
     */
    public void addELContextListener(ELContextListener listener) {
        if (listener != null) {
            elContextListeners.add(listener);
        }
    }

    /*
     * @see jakarta.faces.application.Application#removeELContextListener(jakarta.el.ELContextListener)
     */
    public void removeELContextListener(ELContextListener listener) {
        if (listener != null) {
            elContextListeners.remove(listener);
        }
    }

    /*
     * @see jakarta.faces.application.Application#getELContextListeners()
     */
    public ELContextListener[] getELContextListeners() {
        if (!elContextListeners.isEmpty()) {
            return elContextListeners.toArray(new ELContextListener[elContextListeners.size()]);
        }

        return EMPTY_EL_CTX_LIST_ARRAY;
    }

    /*
     * @see jakarta.faces.application.Application#getELResolver()
     */
    public ELResolver getELResolver() {
        if (compositeELResolver == null) {
            synchronized (this) {
                if (compositeELResolver == null) {
                    performOneTimeELInitialization();
                }
            }
        }

        return compositeELResolver;
    }

    /*
     * @see jakarta.faces.application.Application#addELResolver(jakarta.el.ELResolver)
     */
    public void addELResolver(ELResolver resolver) {
        if (associate.hasRequestBeenServiced()) {
            throw new IllegalStateException(getExceptionMessageString(ILLEGAL_ATTEMPT_SETTING_APPLICATION_ARTIFACT_ID, "ELResolver"));
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELAwareBeanManager cdiBeanManager = getCdiBeanManager(facesContext);

        if (!resolver.equals(cdiBeanManager.getELResolver())) {
            elResolvers.add(resolver);
        }
    }

    /*
     * @see jakarta.faces.application.Application#getExpressionFactory()
     */
    public ExpressionFactory getExpressionFactory() {
        return associate.getExpressionFactory();
    }

    /*
     * @see jakarta.faces.application.Application#evaluateExpressionGet(jakarta.faces.context.FacesContext, String, Class)
     */
    @SuppressWarnings("unchecked")
    public <T> T evaluateExpressionGet(FacesContext context, String expression, Class<? extends T> expectedType) throws ELException {
        return (T) getExpressionFactory().createValueExpression(context.getELContext(), expression, expectedType).getValue(context.getELContext());
    }

    public CompositeELResolver getApplicationELResolvers() {
        return elResolvers;
    }

    public FacesCompositeELResolver getCompositeELResolver() {
        return compositeELResolver;
    }

    public void setCompositeELResolver(FacesCompositeELResolver compositeELResolver) {
        this.compositeELResolver = compositeELResolver;
    }

    private void performOneTimeELInitialization() {
        if (compositeELResolver != null) {
            throw new IllegalStateException("Class invariant invalidated: " + "The Application instance's ELResolver is not null " + "and it should be.");
        }
        associate.initializeELResolverChains();
    }

}
