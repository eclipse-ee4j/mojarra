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

import java.util.Stack;
import java.util.logging.Logger;
import java.util.logging.Level;

import jakarta.servlet.jsp.JspException;

import java.lang.reflect.Method;

import com.sun.faces.util.RequestStateManager;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.webapp.UIComponentClassicTagBase;
import jakarta.faces.webapp.UIComponentELTag;

import com.sun.faces.util.ReflectionUtils;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.RIConstants;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class SubviewTag extends UIComponentELTag {

    private static final Logger LOGGER = FacesLogger.TAGLIB.getLogger();

    // ------------------------------------------------------------ Constructors

    public SubviewTag() {

        super();

    }

    // ---------------------------------------------------------- Public Methods

    @Override
    public String getComponentType() {

        return "jakarta.faces.NamingContainer";

    }

    @Override
    public String getRendererType() {

        return null;

    }

    // ------------------------------------------------------- Protected Methods

    @Override
    protected UIComponent createVerbatimComponentFromBodyContent() {

        UIOutput verbatim = (UIOutput) super.createVerbatimComponentFromBodyContent();
        String value = null;

        FacesContext ctx = getFacesContext();
        Object response = ctx.getExternalContext().getResponse();
        // flush out any content above the view tag
        Method customFlush = ReflectionUtils.lookupMethod(response.getClass(), "flushContentToWrappedResponse", RIConstants.EMPTY_CLASS_ARGS);
        Method isBytes = ReflectionUtils.lookupMethod(response.getClass(), "isBytes", RIConstants.EMPTY_CLASS_ARGS);
        Method isChars = ReflectionUtils.lookupMethod(response.getClass(), "isChars", RIConstants.EMPTY_CLASS_ARGS);
        Method resetBuffers = ReflectionUtils.lookupMethod(response.getClass(), "resetBuffers", RIConstants.EMPTY_CLASS_ARGS);
        Method getChars = ReflectionUtils.lookupMethod(response.getClass(), "getChars", RIConstants.EMPTY_CLASS_ARGS);
        boolean cont = true;
        if (isBytes == null) {
            cont = false;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "jsf.core.taglib.subviewtag.interweaving_failed_isbytes");
            }
        }
        if (isChars == null) {
            cont = false;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "jsf.core.taglib.subviewtag.interweaving_failed_ischars");
            }
        }
        if (resetBuffers == null) {
            cont = false;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "jsf.core.taglib.subviewtag.interweaving_failed_resetbuffers");
            }
        }
        if (getChars == null) {
            cont = false;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "jsf.core.taglib.subviewtag.interweaving_failed_getchars");
            }
        }
        if (customFlush == null) {
            cont = false;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "jsf.core.taglib.viewtag.interweaving_failed");
            }
        }

        if (cont) {
            try {
                if ((Boolean) isBytes.invoke(response)) {
                    customFlush.invoke(response);
                } else if ((Boolean) isChars.invoke(response)) {
                    char[] chars = (char[]) getChars.invoke(response);
                    if (null != chars && 0 < chars.length) {
                        if (null != verbatim) {
                            value = (String) verbatim.getValue();
                        }
                        verbatim = super.createVerbatimComponent();
                        if (null != value) {
                            verbatim.setValue(value + new String(chars));
                        } else {
                            verbatim.setValue(new String(chars));
                        }
                    }
                }
                resetBuffers.invoke(response);

            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new FacesException("Response interweaving failed!", e);
            }
        }

        return verbatim;

    }

    @Override
    public int doEndTag() throws JspException {
        int retValue;

        getViewTagStack().pop();
        retValue = super.doEndTag();
        return retValue;
    }

    @Override
    public int doStartTag() throws JspException {
        int retValue;

        retValue = super.doStartTag();
        getViewTagStack().push(this);

        return retValue;
    }

    /**
     * @return Stack of UIComponentClassicTagBase instances, each of which is a "view" tag. The bottom most element on the
     * stack is the ViewTag itself. Subsequent instances are SubviewTag instances.
     */
    static Stack<UIComponentClassicTagBase> getViewTagStack() {

        FacesContext ctx = FacesContext.getCurrentInstance();
        Map<String, Object> stateMap = RequestStateManager.getStateMap(ctx);

        // noinspection unchecked
        Stack<UIComponentClassicTagBase> result = (Stack<UIComponentClassicTagBase>) stateMap.get(RequestStateManager.VIEWTAG_STACK_ATTR_NAME);
        if (result == null) {
            result = new Stack<>();
            stateMap.put(RequestStateManager.VIEWTAG_STACK_ATTR_NAME, result);
        }

        return result;
    }

}
