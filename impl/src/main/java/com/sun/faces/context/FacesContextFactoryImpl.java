/*
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

package com.sun.faces.context;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.AlwaysPerformValidationWhenRequiredTrue;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableValidateWholeBean;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.ForceAlwaysWriteFlashCookie;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.PartialStateSaving;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.ViewRootPhaseListenerQueuesException;

import java.util.Map;

import com.sun.faces.RIConstants;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.ExceptionHandlerFactory;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.ExternalContextFactory;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;

public class FacesContextFactoryImpl extends FacesContextFactory {

    private final ExceptionHandlerFactory exceptionHandlerFactory;
    private final ExternalContextFactory externalContextFactory;

    // ------------------------------------------------------------ Constructors

    public FacesContextFactoryImpl() {
        super(null);

        exceptionHandlerFactory = (ExceptionHandlerFactory) FactoryFinder.getFactory(FactoryFinder.EXCEPTION_HANDLER_FACTORY);
        externalContextFactory = (ExternalContextFactory) FactoryFinder.getFactory(FactoryFinder.EXTERNAL_CONTEXT_FACTORY);

    }

    // ---------------------------------------- Methods from FacesContextFactory

    @Override
    public FacesContext getFacesContext(Object sc, Object request, Object response, Lifecycle lifecycle) throws FacesException {

        Util.notNull("sc", sc);
        Util.notNull("request", request);
        Util.notNull("response", response);
        Util.notNull("lifecycle", lifecycle);
        ExternalContext extContext;

        FacesContext ctx = new FacesContextImpl(extContext = externalContextFactory.getExternalContext(sc, request, response), lifecycle);

        ctx.setExceptionHandler(exceptionHandlerFactory.getExceptionHandler());
        WebConfiguration webConfig = WebConfiguration.getInstance(extContext);

        savePerRequestInitParams(ctx, webConfig);
        return ctx;

    }

    /*
     * Copy the value of any init params that must be checked during this request to our FacesContext attribute map.
     */
    private void savePerRequestInitParams(FacesContext context, WebConfiguration webConfig) {
        ExternalContext extContext = context.getExternalContext();
        Map<String, Object> appMap = extContext.getApplicationMap();
        Map<Object, Object> attrs = context.getAttributes();
        attrs.put(UIInput.ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE,
                webConfig.isOptionEnabled(AlwaysPerformValidationWhenRequiredTrue) ? Boolean.TRUE : Boolean.FALSE);
        attrs.put(PartialStateSaving, webConfig.isOptionEnabled(PartialStateSaving) ? Boolean.TRUE : Boolean.FALSE);
        attrs.put(ForceAlwaysWriteFlashCookie, webConfig.isOptionEnabled(ForceAlwaysWriteFlashCookie) ? Boolean.TRUE : Boolean.FALSE);
        // We must use getQualifiedName here because the consumer is in faces-api
        // and thus cannot import the enum.
        attrs.put(ViewRootPhaseListenerQueuesException.getQualifiedName(),
                webConfig.isOptionEnabled(ViewRootPhaseListenerQueuesException) ? Boolean.TRUE : Boolean.FALSE);
        attrs.put(EnableValidateWholeBean.getQualifiedName(), webConfig.isOptionEnabled(EnableValidateWholeBean) ? Boolean.TRUE : Boolean.FALSE);

        String facesConfigVersion = String.valueOf(appMap.get(RIConstants.FACES_CONFIG_VERSION));
        attrs.put(RIConstants.FACES_CONFIG_VERSION, facesConfigVersion);
    }

    // The testcase for this class is TestSerlvetFacesContextFactory.java

} // end of class FacesContextFactoryImpl
