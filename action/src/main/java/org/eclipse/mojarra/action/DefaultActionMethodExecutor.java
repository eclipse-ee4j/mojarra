/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.action;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

/**
 * The default ActionMethodExecutor.
 */
@ApplicationScoped
public class DefaultActionMethodExecutor implements ActionMethodExecutor {

    /**
     * Stores the ActionParameterProducer.
     */
    @Inject
    private ActionParameterProducer actionParameterProducer;
    
    /**
     * Execute the method.
     *
     * @param facesContext the Faces context.
     * @param actionMappingMatch the action mapping match.
     */
    @Override
    public void execute(FacesContext facesContext, ActionMappingMatch actionMappingMatch) {
        Instance instance = CDI.current().select(
                actionMappingMatch.getBean().getBeanClass(), Any.Literal.INSTANCE);
        String viewId;
        try {
            Object[] parameters = new Object[actionMappingMatch.getMethod().getParameterCount()];
            if (parameters.length > 0) {
                for(int i=0; i<parameters.length; i++) {
                    parameters[i] = actionParameterProducer.produce(
                            facesContext,
                            actionMappingMatch,
                            actionMappingMatch.getMethod().getParameterTypes()[i],
                            actionMappingMatch.getMethod().getParameterAnnotations()[i]);
                }
            }
            viewId = (String) actionMappingMatch.getMethod().invoke(
                    instance.get(), parameters);
        } catch (Throwable throwable) {
            throw new FacesException(throwable);
        }
        if (facesContext.getViewRoot() == null) {
            UIViewRoot viewRoot = facesContext.getApplication().getViewHandler().createView(facesContext, viewId);
            facesContext.setViewRoot(viewRoot);
        }
    }
}
