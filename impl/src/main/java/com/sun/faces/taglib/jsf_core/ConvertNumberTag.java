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

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.NumberConverter;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.el.ExpressionFactory;
import jakarta.servlet.jsp.JspException;

import com.sun.faces.el.ELUtils;

/**
 * <p>
 * ConvertNumberTag is a ConverterTag implementation for jakarta.faces.convert.NumberConverter
 * </p>
 *
 */

public class ConvertNumberTag extends AbstractConverterTag {

    private static final long serialVersionUID = -2710405278792415110L;
    private static ValueExpression CONVERTER_ID_EXPR = null;

    private static final Logger LOGGER = FacesLogger.TAGLIB.getLogger();

    //
    // Instance Variables
    //

    private ValueExpression currencyCodeExpression;
    private ValueExpression currencySymbolExpression;
    private ValueExpression groupingUsedExpression;
    private ValueExpression integerOnlyExpression;
    private ValueExpression maxFractionDigitsExpression;
    private ValueExpression maxIntegerDigitsExpression;
    private ValueExpression minFractionDigitsExpression;
    private ValueExpression minIntegerDigitsExpression;
    private ValueExpression localeExpression;
    private ValueExpression patternExpression;
    private ValueExpression typeExpression;

    private String currencyCode;
    private String currencySymbol;
    private boolean groupingUsed;
    private boolean integerOnly;
    private int maxFractionDigits;
    private int maxIntegerDigits;
    private int minFractionDigits;
    private int minIntegerDigits;
    private Locale locale;
    private String pattern;
    private String type;

    private boolean maxFractionDigitsSpecified;
    private boolean maxIntegerDigitsSpecified;
    private boolean minFractionDigitsSpecified;
    private boolean minIntegerDigitsSpecified;

    // Attribute Instance Variables

    // Relationship Instance Variables

    //
    // Constructors and Initializers
    //
    public ConvertNumberTag() {
        super();
        init();
    }

    @Override
    public void release() {
        super.release();
        init();
    }

    private void init() {
        currencyCode = null;
        currencyCodeExpression = null;
        currencySymbol = null;
        currencySymbolExpression = null;
        groupingUsed = true;
        groupingUsedExpression = null;
        integerOnly = false;
        integerOnlyExpression = null;
        maxFractionDigits = 0;
        maxFractionDigitsExpression = null;
        maxFractionDigitsSpecified = false;
        maxIntegerDigits = 0;
        maxIntegerDigitsExpression = null;
        maxIntegerDigitsSpecified = false;
        minFractionDigits = 0;
        minFractionDigitsExpression = null;
        minFractionDigitsSpecified = false;
        minIntegerDigits = 0;
        minIntegerDigitsExpression = null;
        minIntegerDigitsSpecified = false;
        locale = null;
        localeExpression = null;
        pattern = null;
        patternExpression = null;
        type = "number";
        typeExpression = null;
        if (CONVERTER_ID_EXPR == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            ExpressionFactory factory = context.getApplication().getExpressionFactory();
            CONVERTER_ID_EXPR = factory.createValueExpression(context.getELContext(), "jakarta.faces.Number", String.class);
        }
    }

    //
    // Class methods
    //

    //
    // General Methods
    //

    public void setCurrencyCode(ValueExpression currencyCode) {
        currencyCodeExpression = currencyCode;
    }

    public void setCurrencySymbol(ValueExpression currencySymbol) {
        currencySymbolExpression = currencySymbol;
    }

    public void setGroupingUsed(ValueExpression groupingUsed) {
        groupingUsedExpression = groupingUsed;
    }

    public void setIntegerOnly(ValueExpression integerOnly) {
        integerOnlyExpression = integerOnly;
    }

    public void setMaxFractionDigits(ValueExpression maxFractionDigits) {
        maxFractionDigitsExpression = maxFractionDigits;
        maxFractionDigitsSpecified = true;
    }

    public void setMaxIntegerDigits(ValueExpression maxIntegerDigits) {
        maxIntegerDigitsExpression = maxIntegerDigits;
        maxIntegerDigitsSpecified = true;
    }

    public void setMinFractionDigits(ValueExpression minFractionDigits) {
        minFractionDigitsExpression = minFractionDigits;
        minFractionDigitsSpecified = true;
    }

    public void setMinIntegerDigits(ValueExpression minIntegerDigits) {
        minIntegerDigitsExpression = minIntegerDigits;
    }

    public void setLocale(ValueExpression locale) {
        localeExpression = locale;
    }

    public void setPattern(ValueExpression pattern) {
        patternExpression = pattern;
    }

    public void setType(ValueExpression type) {
        typeExpression = type;
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

        NumberConverter result = (NumberConverter) super.createConverter();
        assert null != result;

        evaluateExpressions();
        result.setCurrencyCode(currencyCode);
        result.setCurrencySymbol(currencySymbol);
        result.setGroupingUsed(groupingUsed);
        result.setIntegerOnly(integerOnly);
        if (maxFractionDigitsSpecified) {
            result.setMaxFractionDigits(maxFractionDigits);
        }
        if (maxIntegerDigitsSpecified) {
            result.setMaxIntegerDigits(maxIntegerDigits);
        }
        if (minFractionDigitsSpecified) {
            result.setMinFractionDigits(minFractionDigits);
        }
        if (minIntegerDigitsSpecified) {
            result.setMinIntegerDigits(minIntegerDigits);
        }
        result.setLocale(locale);
        result.setPattern(pattern);
        result.setType(type);

        return result;
    }

    /* Evaluates expressions as necessary */
    private void evaluateExpressions() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();

        if (currencyCodeExpression != null) {
            currencyCode = (String) ELUtils.evaluateValueExpression(currencyCodeExpression, elContext);
        }
        if (currencySymbolExpression != null) {
            currencySymbol = (String) ELUtils.evaluateValueExpression(currencySymbolExpression, elContext);
        }
        if (patternExpression != null) {
            pattern = (String) ELUtils.evaluateValueExpression(patternExpression, elContext);
        }
        if (typeExpression != null) {
            type = (String) ELUtils.evaluateValueExpression(typeExpression, elContext);
        }
        if (groupingUsedExpression != null) {
            if (groupingUsedExpression.isLiteralText()) {
                groupingUsed = Boolean.valueOf(groupingUsedExpression.getExpressionString()).booleanValue();
            } else {
                groupingUsed = ((Boolean) ELUtils.evaluateValueExpression(groupingUsedExpression, elContext)).booleanValue();
            }
        }
        if (integerOnlyExpression != null) {
            if (integerOnlyExpression.isLiteralText()) {
                integerOnly = Boolean.valueOf(integerOnlyExpression.getExpressionString()).booleanValue();
            } else {
                integerOnly = ((Boolean) ELUtils.evaluateValueExpression(integerOnlyExpression, elContext)).booleanValue();
            }
        }
        if (maxFractionDigitsExpression != null) {
            if (maxFractionDigitsExpression.isLiteralText()) {
                maxFractionDigits = Integer.valueOf(maxFractionDigitsExpression.getExpressionString()).intValue();
            } else {
                maxFractionDigits = ((Integer) ELUtils.evaluateValueExpression(maxFractionDigitsExpression, elContext)).intValue();
            }
        }
        if (maxIntegerDigitsExpression != null) {
            if (maxIntegerDigitsExpression.isLiteralText()) {
                maxIntegerDigits = Integer.valueOf(maxIntegerDigitsExpression.getExpressionString()).intValue();
            } else {
                maxIntegerDigits = ((Integer) ELUtils.evaluateValueExpression(maxIntegerDigitsExpression, elContext)).intValue();
            }
        }
        if (minFractionDigitsExpression != null) {
            if (minFractionDigitsExpression.isLiteralText()) {
                minFractionDigits = Integer.valueOf(minFractionDigitsExpression.getExpressionString()).intValue();
            } else {
                minFractionDigits = ((Integer) ELUtils.evaluateValueExpression(minFractionDigitsExpression, elContext)).intValue();
            }
        }
        if (minIntegerDigitsExpression != null) {
            if (minIntegerDigitsExpression.isLiteralText()) {
                minIntegerDigits = Integer.valueOf(minIntegerDigitsExpression.getExpressionString()).intValue();
            } else {
                minIntegerDigits = ((Integer) ELUtils.evaluateValueExpression(minIntegerDigitsExpression, elContext)).intValue();
            }
        }

        if (localeExpression != null) {
            if (localeExpression.isLiteralText()) {
                locale = getLocale(localeExpression.getExpressionString());
            } else {
                Object loc = ELUtils.evaluateValueExpression(localeExpression, elContext);
                if (loc != null) {
                    if (loc instanceof String) {
                        locale = getLocale((String) loc);
                    } else if (loc instanceof Locale) {
                        locale = (Locale) loc;
                    } else {
                        Object[] params = { "locale", "java.lang.String or java.util.Locale", loc.getClass().getName() };
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE, "jsf.core.tags.eval_result_not_expected_type", params);
                        }
                        throw new FacesException(MessageUtils.getExceptionMessageString(MessageUtils.EVAL_ATTR_UNEXPECTED_TYPE, params));
                    }
                } else {
                    locale = facesContext.getViewRoot().getLocale();
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
                LOGGER.log(Level.WARNING, "jsf.core.taglib.invalid_locale_value", string);
            }
        } else {
            String[] langs = Locale.getISOLanguages();
            Arrays.sort(langs);
            if (Arrays.binarySearch(langs, string) < 0) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "jsf.core.taglib.invalid_language", string);
                }
            }
        }

        return new Locale(string, "");
    }

} // end of class ConvertNumberTag
