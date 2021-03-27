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

import com.sun.faces.facelets.tag.faces.ComponentSupport;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.NumberConverter;
import jakarta.faces.view.facelets.ConverterConfig;
import jakarta.faces.view.facelets.ConverterHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.TagAttribute;

/**
 * Register a NumberConverter instance on the UIComponent associated with the closest parent UIComponent custom action.
 * <p/>
 * See <a target="_new" href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/convertNumber.html">tag
 * documentation</a>.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class ConvertNumberHandler extends ConverterHandler {

    private final TagAttribute locale;

    /**
     * @param config
     */
    public ConvertNumberHandler(ConverterConfig config) {
        super(config);
        locale = getAttribute("locale");
    }

    /**
     * Returns a new NumberConverter
     *
     * @see NumberConverter
     */
    protected Converter createConverter(FaceletContext ctx) throws FacesException, ELException, FaceletException {
        return ctx.getFacesContext().getApplication().createConverter(NumberConverter.CONVERTER_ID);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.tag.ObjectHandler#setAttributes(com.sun.facelets.FaceletContext, java.lang.Object)
     */
    @Override
    public void setAttributes(FaceletContext ctx, Object obj) {
        super.setAttributes(ctx, obj);
        NumberConverter c = (NumberConverter) obj;
        if (locale != null) {
            c.setLocale(ComponentSupport.getLocale(ctx, locale));
        }
    }

    @Override
    public MetaRuleset createMetaRuleset(Class type) {
        return super.createMetaRuleset(type).ignore("locale");
    }

}
