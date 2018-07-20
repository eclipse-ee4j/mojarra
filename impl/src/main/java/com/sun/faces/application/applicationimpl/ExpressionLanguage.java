/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.el.CompositeELResolver;
import javax.el.ELContextListener;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.el.FacesCompositeELResolver;

public class ExpressionLanguage {

    private static final ELContextListener[] EMPTY_EL_CTX_LIST_ARRAY = {};

    private final ApplicationAssociate associate;


    private List<ELContextListener> elContextListeners;
    private CompositeELResolver elResolvers;
    private FacesCompositeELResolver compositeELResolver;

    private Version version = new Version();

    public ExpressionLanguage(ApplicationAssociate applicationAssociate) {
        this.associate = applicationAssociate;

        elContextListeners = new CopyOnWriteArrayList<>();
        elResolvers = new CompositeELResolver();
    }

    /**
     * @see javax.faces.application.Application#addELContextListener(javax.el.ELContextListener)
     */
    public void addELContextListener(ELContextListener listener) {
        if (listener != null) {
            elContextListeners.add(listener);
        }
    }

    /**
     * @see javax.faces.application.Application#removeELContextListener(javax.el.ELContextListener)
     */
    public void removeELContextListener(ELContextListener listener) {
        if (listener != null) {
            elContextListeners.remove(listener);
        }
    }

    /**
     * @see javax.faces.application.Application#getELContextListeners()
     */
    public ELContextListener[] getELContextListeners() {
        if (!elContextListeners.isEmpty()) {
            return elContextListeners.toArray(new ELContextListener[elContextListeners.size()]);
        }

        return EMPTY_EL_CTX_LIST_ARRAY;
    }

    /**
     * @see javax.faces.application.Application#getELResolver()
     */
    public ELResolver getELResolver() {

        if (compositeELResolver == null) {
            performOneTimeELInitialization();
        }

        return compositeELResolver;
    }

    /**
     * @see javax.faces.application.Application#addELResolver(javax.el.ELResolver)
     */
    public void addELResolver(ELResolver resolver) {

        if (associate.hasRequestBeenServiced()) {
            throw new IllegalStateException(getExceptionMessageString(ILLEGAL_ATTEMPT_SETTING_APPLICATION_ARTIFACT_ID, "ELResolver"));
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (version.isJsf23()) {

            BeanManager cdiBeanManager = getCdiBeanManager(facesContext);

            if (cdiBeanManager != null && !resolver.equals(cdiBeanManager.getELResolver())) {
                elResolvers.add(resolver);
            }
        } else {
            elResolvers.add(resolver);
        }
    }

    /**
     * @see javax.faces.application.Application#getExpressionFactory()
     */
    public ExpressionFactory getExpressionFactory() {
        return associate.getExpressionFactory();
    }

    /**
     * @see javax.faces.application.Application#evaluateExpressionGet(javax.faces.context.FacesContext,
     *      String, Class)
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
