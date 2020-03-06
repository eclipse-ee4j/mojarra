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

package com.sun.faces.taglib.jsf_core;

import com.sun.faces.el.ELUtils;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.DateTimeConverter;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.servlet.jsp.JspException;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p>ConvertDateTimeTag is a ConverterTag implementation for
 * jakarta.faces.convert.DateTimeConverter</p>
 *
 */

public class ConvertDateTimeTag extends AbstractConverterTag {

    private static final long serialVersionUID = -5815655767093677438L;
    private static ValueExpression CONVERTER_ID_EXPR = null;

     private static final Logger LOGGER = FacesLogger.TAGLIB.getLogger();

    //
    // Instance Variables
    //

    private ValueExpression dateStyleExpression;
    private ValueExpression localeExpression;
    private ValueExpression patternExpression;
    private ValueExpression timeStyleExpression;
    private ValueExpression timeZoneExpression;
    private ValueExpression typeExpression;

    private String dateStyle;
    private Locale locale;
    private String pattern;
    private String timeStyle;
    private TimeZone timeZone;
    private String type;// Log instance for this class


    // Attribute Instance Variables

    // Relationship Instance Variables

    //
    // Constructors and Initializers    
    //
    public ConvertDateTimeTag() {
        super();
        init();
    }


    @Override
    public void release() {
        super.release();
        init();
    }


    private void init() {
        dateStyle = "default";
        dateStyleExpression = null;
        locale = null;
        localeExpression = null;
        pattern = null;
        patternExpression = null;
        timeStyle = "default";
        timeStyleExpression = null;
        timeZone = null;
        timeZoneExpression = null;
        type = "date";
        typeExpression = null;
        if (CONVERTER_ID_EXPR == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            ExpressionFactory factory = context.getApplication().
                    getExpressionFactory();
            CONVERTER_ID_EXPR = factory.createValueExpression(
                    context.getELContext(),"jakarta.faces.DateTime",String.class);
        }
    }

    //
    // Class methods
    //

    //
    // General Methods
    //

    public void setDateStyle(ValueExpression dateStyle) {
        this.dateStyleExpression = dateStyle;
    }


    public void setLocale(ValueExpression locale) {
        this.localeExpression = locale;
    }


    public void setPattern(ValueExpression pattern) {
        this.patternExpression = pattern;
    }


    public void setTimeStyle(ValueExpression timeStyle) {
        this.timeStyleExpression = timeStyle;
    }


    public void setTimeZone(ValueExpression timeZone) {
        this.timeZoneExpression = timeZone;
    }


    public void setType(ValueExpression type) {
        this.typeExpression = type;
    }

    @Override
    public int doStartTag() throws JspException {
        super.setConverterId(CONVERTER_ID_EXPR);
        return super.doStartTag();
    }

    //
    // Methods from ConverterTag
    //

    @Override
    protected Converter createConverter() throws JspException {

        DateTimeConverter result = (DateTimeConverter) super.createConverter();
        assert (null != result);

        evaluateExpressions();
        result.setDateStyle(dateStyle);
        result.setLocale(locale);
        result.setPattern(pattern);
        result.setTimeStyle(timeStyle);
        result.setTimeZone(timeZone);
        result.setType(type);

        return result;
    }


    /* Evaluates expressions as necessary */
    private void evaluateExpressions() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();

        if (dateStyleExpression != null) {
            dateStyle = (String)
            ELUtils.evaluateValueExpression(dateStyleExpression, elContext);
        }
        if (patternExpression != null) {
            pattern = (String)
            ELUtils.evaluateValueExpression(patternExpression, elContext);
        }
        if (timeStyleExpression != null) {
            timeStyle = (String)
            ELUtils.evaluateValueExpression(timeStyleExpression, elContext);
        }
        if (typeExpression != null) {
            type = (String)
            ELUtils.evaluateValueExpression(typeExpression, elContext);
        } else {
            if (timeStyleExpression != null) {
                if (dateStyleExpression != null) {
                    type = "both";
                } else {
                    type = "time";
                }
            } else {
                type = "date";
            }
        }
        if (localeExpression != null) {
            if (localeExpression.isLiteralText()) {
                locale = getLocale(localeExpression.getExpressionString());
            } else {
                Object loc = ELUtils.evaluateValueExpression(localeExpression,
                                                          elContext);
                if (loc != null) {
                    if (loc instanceof String) {
                        locale = getLocale((String) loc);
                    } else if (loc instanceof Locale) {
                        locale = (Locale) loc;
                    } else {
                        Object[] params = {
                            "locale",
                            "java.lang.String or java.util.Locale",
                            loc.getClass().getName()
                        };
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE,
                                       "jsf.core.tags.eval_result_not_expected_type",
                                       params);
                        }
                        throw new FacesException(
                            MessageUtils.getExceptionMessageString(
                                MessageUtils.EVAL_ATTR_UNEXPECTED_TYPE, params));
                    }
                } else {
                    locale = facesContext.getViewRoot().getLocale();
                }
            }
        }
        if (timeZoneExpression != null) {
            if (timeZoneExpression.isLiteralText()) {
                timeZone =
                TimeZone.getTimeZone(
                    timeZoneExpression.getExpressionString());
            } else {
                Object tz = ELUtils.evaluateValueExpression(timeZoneExpression,
                                                         elContext);
                if (tz != null) {
                    if (tz instanceof String) {
                        timeZone = TimeZone.getTimeZone((String) tz);
                    } else if (tz instanceof TimeZone) {
                        timeZone = (TimeZone) tz;
                    } else {
                        Object[] params = {
                            "timeZone",
                            "java.lang.String or java.util.TimeZone",
                            tz.getClass().getName()
                        };
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE,
                                       "jsf.core.tags.eval_result_not_expected_type",
                                       params);
                        }
                        throw new FacesException(
                            MessageUtils.getExceptionMessageString(
                                MessageUtils.EVAL_ATTR_UNEXPECTED_TYPE, params));
                    }
                }
            }
        }
    }

    protected static Locale getLocale(String string) {
        if (string == null) {
            return Locale.getDefault();
        }

        if (string.length() > 2) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING,
                           "jsf.core.taglib.invalid_locale_value",
                           string);
            }
        } else {
            String[] langs = Locale.getISOLanguages();
            Arrays.sort(langs);
            if (Arrays.binarySearch(langs, string) < 0) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING,
                               "jsf.core.taglib.invalid_language",
                               string);
                }
            }
        }

        return new Locale(string, "");
    }
} // end of class ConvertDateTimeTag
