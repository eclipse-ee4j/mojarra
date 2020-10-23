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

package com.sun.faces.taglib.jsf_core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.ReflectionUtils;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.webapp.UIComponentClassicTagBase;
import jakarta.faces.webapp.UIComponentELTag;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.jstl.core.Config;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTag;

/**
 * All Faces component tags must be nested within a f:view tag. This tag corresponds to the root of the UIComponent tree.
 * It does not have a Renderer. It exists mainly to provide a guarantee that all faces components reside inside of this
 * tag.
 *
 */
public class ViewTag extends UIComponentELTag {

    //
    // Protected Constants
    //

    //
    // Class Variables
    //

    private static final Logger LOGGER = FacesLogger.TAGLIB.getLogger();

    //
    // Instance Variables
    //

    // Attribute Instance Variables

    protected ValueExpression renderKitId = null;

    public void setRenderKitId(ValueExpression renderKitId) {
        this.renderKitId = renderKitId;
    }

    protected ValueExpression locale = null;

    public void setLocale(ValueExpression newLocale) {
        locale = newLocale;
    }

    protected MethodExpression beforePhase = null;

    public void setBeforePhase(MethodExpression newBeforePhase) {
        beforePhase = newBeforePhase;
    }

    protected MethodExpression afterPhase = null;

    public void setAfterPhase(MethodExpression newAfterPhase) {
        afterPhase = newAfterPhase;
    }

    // Relationship Instance Variables

    //
    // Constructors and Initializers
    //

    public ViewTag() {
        super();
    }

    //
    // Class methods
    //

    //
    // Accessors
    //

    //
    // General Methods
    //

    @Override
    protected int getDoStartValue() throws JspException {
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    /**
     * <p>
     * Override parent <code>doStartTag()</code> to do the following:
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * Reflect the response object for a method called flushContentToWrappedResponse and invoke it. This causes any content
     * that appears before the view to be written out to the response. This is necessary to allow proper ordering to happen.
     * </p>
     * </li>
     *
     * </ul>
     *
     */

    @Override
    public int doStartTag() throws JspException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            throw new IllegalStateException(MessageUtils.getExceptionMessageString(MessageUtils.FACES_CONTEXT_NOT_FOUND_ID));
        }

        // flush out any content above the view tag
        Object response = facesContext.getExternalContext().getResponse();
        Method customFlush = ReflectionUtils.lookupMethod(response.getClass(), "flushContentToWrappedResponse", RIConstants.EMPTY_CLASS_ARGS);
        if (customFlush != null) {
            try {
                pageContext.getOut().flush();
                customFlush.invoke(response, RIConstants.EMPTY_METH_ARGS);
            } catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new JspException("Exception attemtping to write content above the <f:view> tag.", e);
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "jsf.core.taglib.viewtag.interweaving_failed");
            }
        }

        int rc;
        try {
            rc = super.doStartTag();
        } catch (JspException e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "Can't leverage base class", e);
            }
            throw e;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "Can't leverage base class", t);
            }
            throw new JspException(t);
        }

        // this must happen after our overriderProperties executes.
        pageContext.getResponse().setLocale(facesContext.getViewRoot().getLocale());

        List<UIComponent> preViewLoadBundleComponents = LoadBundleTag.getPreViewLoadBundleComponentList();
        if (!preViewLoadBundleComponents.isEmpty()) {
            Iterator<UIComponent> iter = preViewLoadBundleComponents.iterator();
            UIComponent cur;
            while (iter.hasNext()) {
                cur = iter.next();
                LoadBundleTag.addChildToParentTagAndParentComponent(cur, this);
            }
            preViewLoadBundleComponents.clear();
        }
        Stack<UIComponentClassicTagBase> viewTagStack = SubviewTag.getViewTagStack();
        viewTagStack.push(this);
        return rc;
    }

    /**
     * <p>
     * Examine the body content of this tag. If it is non-<code>null</code>, non-zero length, and not an HTML comment, call
     * {@link jakarta.faces.webapp.UIComponentClassicTagBase#createVerbatimComponent()}.
     * </p>
     *
     * <p>
     * Set the value of the verbatim component to be <code>content</code>.
     * </p>
     *
     * <p>
     * Add this child to the end of the child list for <code>UIViewRoot</code>.
     * </p>
     */

    @Override
    public int doAfterBody() throws JspException {
        int result = EVAL_PAGE;
        BodyContent bodyContent;
        UIViewRoot root = FacesContext.getCurrentInstance().getViewRoot();
        UIOutput verbatim;
        String content;
        String trimContent;

        Stack<UIComponentClassicTagBase> viewTagStack = SubviewTag.getViewTagStack();
        viewTagStack.pop();

        if (null == (bodyContent = getBodyContent()) || null == (content = bodyContent.getString()) || 0 == (trimContent = content.trim()).length()
                || trimContent.startsWith("<!--") && trimContent.endsWith("-->")) {
            return result;
        }

        bodyContent.clearBody();

        verbatim = createVerbatimComponent();
        verbatim.setValue(content);

        root.getChildren().add(verbatim);

        return result;
    }

    /**
     * <p>
     * Exercise a contract with the {@link ViewHandler} to get the character encoding from the response and set it into the
     * session.
     * </p>
     */

    @Override
    public int doEndTag() throws JspException {
        int rc = super.doEndTag();
        // store the response character encoding
        HttpSession session;

        if (null != (session = pageContext.getSession())) {
            session.setAttribute(ViewHandler.CHARACTER_ENCODING_KEY, pageContext.getResponse().getCharacterEncoding());
        }
        return rc;
    }

    @Override
    public String getComponentType() {
        return UIViewRoot.COMPONENT_TYPE;
    }

    @Override
    public String getRendererType() {
        return null;
    }

    @Override
    protected int getDoEndValue() throws JspException {
        return EVAL_PAGE;
    }

    //
    // Methods from Superclass
    //
    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        Locale viewLocale = null;
        UIViewRoot viewRoot = (UIViewRoot) component;
        FacesContext context = FacesContext.getCurrentInstance();
        ELContext elContext = context.getELContext();
        try {

            if (null != renderKitId) {
                if (renderKitId.isLiteralText()) {
                    // PENDING(edburns): better error message than NPE
                    // possible here.
                    viewRoot.setRenderKitId(renderKitId.getValue(elContext).toString());
                } else {
                    // clear out the literal value to force using the
                    // expression
                    viewRoot.setRenderKitId(null);
                    viewRoot.setValueExpression("renderKitId", renderKitId);
                }
            } else if (viewRoot.getRenderKitId() == null) {
                String renderKitIdString = context.getApplication().getDefaultRenderKitId();
                if (null == renderKitIdString) {
                    renderKitIdString = RenderKitFactory.HTML_BASIC_RENDER_KIT;
                }
                viewRoot.setRenderKitId(renderKitIdString);
            }

            if (null != locale) {
                if (locale.isLiteralText()) {
                    // PENDING(edburns): better error message than NPE
                    // possible here.
                    viewLocale = getLocaleFromString(locale.getValue(elContext).toString());
                } else {
                    component.setValueExpression("locale", locale);
                    Object result = locale.getValue(context.getELContext());
                    if (result instanceof Locale) {
                        viewLocale = (Locale) result;
                    } else if (result instanceof String) {
                        viewLocale = getLocaleFromString((String) result);
                    }
                }
            }
            // BUGDB 10235218
            if (null != viewLocale) {
                ((UIViewRoot) component).setLocale(viewLocale);
                // update the JSTL locale attribute in request scope so that
                // JSTL picks up the locale from viewRoot. This attribute
                // must be updated before the JSTL setBundle tag is called
                // because that is when the new LocalizationContext object
                // is created based on the locale.
                Config.set(pageContext.getRequest(), Config.FMT_LOCALE, viewLocale);
            }

            if (null != beforePhase) {
                if (beforePhase.isLiteralText()) {
                    Object params[] = { beforePhase };
                    throw new jakarta.faces.FacesException(MessageUtils.getExceptionMessageString(MessageUtils.INVALID_EXPRESSION_ID, params));
                } else {
                    viewRoot.setBeforePhaseListener(beforePhase);

                }
            }
            if (null != afterPhase) {
                if (afterPhase.isLiteralText()) {
                    Object params[] = { afterPhase };
                    throw new jakarta.faces.FacesException(MessageUtils.getExceptionMessageString(MessageUtils.INVALID_EXPRESSION_ID, params));
                } else {
                    viewRoot.setAfterPhaseListener(afterPhase);
                }
            }
        } catch (ELException ele) {
            throw new FacesException(ele);
        }
    }

    /**
     * Returns the locale represented by the expression.
     *
     * @param localeExpr a String in the format specified by JSTL Specification as follows: "A String value is interpreted
     * as the printable representation of a locale, which must contain a two-letter (lower-case) language code (as defined
     * by ISO-639), and may contain a two-letter (upper-case) country code (as defined by ISO-3166). Language and country
     * codes must be separated by hyphen (???-???) or underscore (???_???)."
     * @return Locale instance cosntructed from the expression.
     */
    protected Locale getLocaleFromString(String localeExpr) {
        Locale result = Locale.getDefault();
        if (localeExpr.indexOf("_") == -1 && localeExpr.indexOf("-") == -1) {
            // expression has just language code in it. make sure the
            // expression contains exactly 2 characters.
            if (localeExpr.length() == 2) {
                result = new Locale(localeExpr, "");
            }
        } else {
            // expression has country code in it. make sure the expression
            // contains exactly 5 characters.
            if (localeExpr.length() == 5) {
                // get the language and country to construct the locale.
                String language = localeExpr.substring(0, 2);
                String country = localeExpr.substring(3, localeExpr.length());
                result = new Locale(language, country);
            }
        }
        return result;
    }

} // end of class ViewTag
