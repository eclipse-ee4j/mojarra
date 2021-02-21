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

package com.sun.faces.facelets.tag.faces.core;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.convert.Converter;
import jakarta.faces.view.facelets.ConverterConfig;
import jakarta.faces.view.facelets.ConverterHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.TagAttribute;

/**
 * Register a named Converter instance on the UIComponent associated with the closest parent UIComponent custom action.
 * <p/>
 * See <a target="_new" href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/converter.html">tag
 * documentation</a>.
 *
 * @author Jacob Hookom
 */
public final class ConvertDelegateHandler extends ConverterHandler {

    private final TagAttribute converterId;

    /**
     * @param config
     */
    public ConvertDelegateHandler(ConverterConfig config) {
        super(config);
        converterId = getAttribute("converterId");
    }

    /**
     * Uses the specified "converterId" to pull an instance from the Application
     *
     * @see jakarta.faces.application.Application#createComponent(java.lang.String)
     */
    protected Converter createConverter(FaceletContext ctx) throws FacesException, ELException, FaceletException {
        return ctx.getFacesContext().getApplication().createConverter(converterId.getValue(ctx));
    }

    @Override
    protected MetaRuleset createMetaRuleset(Class type) {
        return super.createMetaRuleset(type).ignoreAll();
    }
}
