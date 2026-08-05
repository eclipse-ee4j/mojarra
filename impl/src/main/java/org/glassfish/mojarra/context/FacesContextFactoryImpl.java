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

package org.glassfish.mojarra.context;


import java.util.List;
import java.util.Map;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.context.ExceptionHandlerFactory;
import jakarta.faces.context.ExternalContextFactory;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;

import org.glassfish.mojarra.util.Util;

public class FacesContextFactoryImpl extends FacesContextFactory {

    private final ExceptionHandlerFactory exceptionHandlerFactory;

    /**
     * The parameters which have to be published into the FacesContext attribute map under their own name, because the
     * API reads them there rather than from the configuration they are resolved in, which it cannot reach. This is an
     * arrangement between the API and an implementation rather than anything the specification states, and it is
     * visible only from private members of UIInput, UIViewRoot and MultiFieldValidationUtils.
     *
     * Only UIInput falls back to reading the parameter itself when the attribute is absent. Dropping either of the
     * other two silently disables the behaviour it guards, so a parameter leaves this list only once the API stops
     * reading it there. A parameter this implementation reads itself never belonged here to begin with.
     */
    private static final List<FacesContextParam> API_READS_FROM_THE_CONTEXT = List.of(
            FacesContextParam.ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE,
            FacesContextParam.ENABLE_VALIDATE_WHOLE_BEAN,
            FacesContextParam.VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS);

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

        FacesContext ctx = new FacesContextImpl(externalContextFactory.getExternalContext(sc, request, response), lifecycle);

        ctx.setExceptionHandler(exceptionHandlerFactory.getExceptionHandler());

        savePerRequestInitParams(ctx);
        return ctx;

    }

    /*
     * Copies the parameters which the API reads back out of the FacesContext attribute map, keyed by the name of the
     * parameter, because it cannot reach the configuration these are resolved in. A parameter this implementation
     * reads itself does not belong here, it consults that configuration directly.
     */
    private void savePerRequestInitParams(FacesContext context) {
        Map<Object, Object> attrs = context.getAttributes();

        for (FacesContextParam param : API_READS_FROM_THE_CONTEXT) {
            attrs.put(param.getName(), param.isEnabled(context));
        }
    }

    // The testcase for this class is TestSerlvetFacesContextFactory.java

} // end of class FacesContextFactoryImpl
