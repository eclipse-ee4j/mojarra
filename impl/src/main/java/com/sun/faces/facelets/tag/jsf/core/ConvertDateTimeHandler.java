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

package com.sun.faces.facelets.tag.jsf.core;

import com.sun.faces.facelets.tag.jsf.ComponentSupport;

import jakarta.faces.FacesException;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.DateTimeConverter;
import jakarta.faces.view.facelets.*;

import jakarta.el.ELException;

import java.util.TimeZone;

/**
 * Register a DateTimeConverter instance on the UIComponent associated with the
 * closest parent UIComponent custom action. <p/> See <a target="_new"
 * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/convertDateTime.html">tag
 * documentation</a>.
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public final class ConvertDateTimeHandler extends ConverterHandler {

    private final TagAttribute dateStyle;

    private final TagAttribute locale;

    private final TagAttribute pattern;

    private final TagAttribute timeStyle;

    private final TagAttribute timeZone;

    private final TagAttribute type;

    /**
     * @param config
     */
    public ConvertDateTimeHandler(ConverterConfig config) {
        super(config);
        this.dateStyle = this.getAttribute("dateStyle");
        this.locale = this.getAttribute("locale");
        this.pattern = this.getAttribute("pattern");
        this.timeStyle = this.getAttribute("timeStyle");
        this.timeZone = this.getAttribute("timeZone");
        this.type = this.getAttribute("type");
    }

    /**
     * Returns a new DateTimeConverter
     * 
     * @see DateTimeConverter
     */
    protected Converter createConverter(FaceletContext ctx)
            throws FacesException, ELException, FaceletException {
        return ctx.getFacesContext().getApplication().createConverter(DateTimeConverter.CONVERTER_ID);

    }

    /**
     * Implements tag spec, see taglib documentation.
     */
    @Override
    public void setAttributes(FaceletContext ctx, Object obj) {
        DateTimeConverter c = (DateTimeConverter) obj;
        if (this.locale != null) {
            c.setLocale(ComponentSupport.getLocale(ctx, this.locale));
        }
        if (this.pattern != null) {
            c.setPattern(this.pattern.getValue(ctx));
            // JAVASERVERFACES_SPEC_PUBLIC-1370 Allow pattern and type to co-exist
            // for java.time values
            if (this.type != null) {
                String typeStr = this.type.getValue(ctx);
                if (isJavaTimeType(typeStr)) {
                    c.setType(typeStr);
                }
            }
            
        } else {
            if (this.type != null) {
                c.setType(this.type.getValue(ctx));
            }
            if (this.dateStyle != null) {
                c.setDateStyle(this.dateStyle.getValue(ctx));
            }
            if (this.timeStyle != null) {
                c.setTimeStyle(this.timeStyle.getValue(ctx));
            }
        }
        
        if (this.timeZone != null) {
            Object t = this.timeZone.getObject(ctx);
            if(t != null) {
	            if (t instanceof TimeZone) {
	                c.setTimeZone((TimeZone) t);
	            } else if (t instanceof String) {
	                TimeZone tz = TimeZone.getTimeZone((String) t);
	                c.setTimeZone(tz);
	            } else {
	                throw new TagAttributeException(
	                        this.tag,
	                        this.timeZone,
	                        "Illegal TimeZone, must evaluate to either a java.util.TimeZone or String, is type: "
	                                + t.getClass());
	            }
            }
        }
    }
    
    private static boolean isJavaTimeType(String type) {
        boolean result = false;
        if (null != type && type.length() > 1) {
            char c = type.charAt(0);
            result = c == 'l' || c == 'o' || c == 'z';
        }
        
        return result;
    }

    @Override
    public MetaRuleset createMetaRuleset(Class type) {
        return super.createMetaRuleset(type).ignoreAll();
    }
}
