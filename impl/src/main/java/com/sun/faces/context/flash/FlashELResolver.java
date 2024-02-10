/*
 * Copyright (c) 2023 Contributors to Eclipse Foundation.
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

package com.sun.faces.context.flash;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.context.FlashFactory;
import java.util.Map;

/**
 * <p>
 * Provide a feature semantically identical to the
 * <a target="_" href="http://api.rubyonrails.com/classes/ActionController/Flash.html"> "flash" concept in Ruby on
 * Rails</a>.
 * </p>
 *
 * <p>
 * The feature is exposed to users via a custom <code>ELResolver</code> which introduces a new implicit object,
 * <code>flash</code>. The flash functions as <code>Map</code> and can be used in <code>getValue( )</code> or
 * <code>setValue(
 * )</code> expressions.
 * </p>
 *
 * <p>
 * Usage
 * </p>
 *
 * <p>
 * Consider three Faces views: viewA, viewB, and viewC. The user first views viewA, then clicks a button and is shown
 * viewB, where she clicks a button and is shown viewC. If values are stored into the flash during the rendering or
 * postback phases of viewA, they are available to during the rendering phase of viewB, but are not available during the
 * rendering or postback phases of viewC. In other words, values stored into the flash on "this" request are accessible
 * for the "next" request, but not thereafter.
 * </p>
 *
 * <p>
 * There are three ways to access the flash.
 * </p>
 *
 * <ol>
 *   <li>
 *     Using an Expression Language Expression, such as using <code>#{flash.foo}</code> as the value of an attribute in a page.
 *   </li>
 *   <li>
 *     Using the EL API, such as:
 *
 *     <p>
 * <code>
 * FacesContext context = FacesContext.getCurrentInstance();
 * ValueExpression flashExpression = context.getApplication().
 *    createValueExpression(context.getELContext(), "#{flash.foo}",
 *                          null, Object.class);
 * flashExpression.setValue(context.getELContext(), "Foo's new value");
 * </code>
 *     </p>
 *
 *   </li>
 *   <li>
 *     <p>
 *       Using getting the {@link ELFlash} directly, such as:
 *     </p>
 *
 *     <p>
 * <code>
 * Map&lt;String,Object&gt; flash = ELFlash.getFlash();
 * flash.put("foo", "Foo's new value");
 * </code>
 *     </p>
 *   </li>
 * </ol>
 *
 * <p>
 * The main entry point to this feature is the first one. This library includes a simple custom tag, <code><a target="_"
 * href="../../../../tlddoc/jsfExt/set.html">jsfExt:set</a></code>, that evaluates an expression and sets its value into
 * another expression. <code>jsfExt:set</code> can be used to store values into the flash from JSP pages, like this:
 * </p>
 *
 * <p>
 * <code>&lt;jsfExt:set var="#{flash.foo}" value="fooValue"
 * /&gt;</code>
 * </p>
 *
 * <p>
 * or this:
 * </p>
 *
 * <p>
 * <code>&lt;jsfExt:set var="#{flash.keep.bar}" value="#{user.name}"
 * /&gt;</code>
 * </p>
 *
 * <p>
 * or even this:
 * </p>
 *
 * <p>
 * <code>
 * &lt;jsfExt:set var="#{flash.now.baz}" value="#{cookie.userCookie}" /&gt;
 *
 * &lt;h:outputText value="#{flash.now.baz}" /&gt;
 *
 * </code>
 * </p>
 *
 * <p>
 * Related Classes
 * </p>
 *
 * <p>
 * The complete list of classes that make up this feature is
 * </p>
 *
 * <ul>
 *   <li><code>FlashELResolver</code></li>
 *   <li><code>{@link ELFlash}</code></li>
 * </ul>
 */
public class FlashELResolver extends ELResolver {

    /**
     * <p>
     * Not intended for manual invocation. Only called by the Faces Runtime.
     * </p>
     */

    public FlashELResolver() {

    }

    // ------------------------------------------------------ Manifest Constants

    private static final String FLASH_VARIABLE_NAME = "flash";

    private static final String FLASH_NOW_VARIABLE_NAME = "now";

    private static final String FLASH_KEEP_VARIABLE_NAME = "keep";

    // ------------------------------------------------ VariableResolver Methods

    /**
     * <p>
     * Hook into the EL resolution process to introduce the <code>flash</code> implicit object. If <code>property</code> is
     * <code>null</code>, take no action and return <code>null</code>. if <code>base</code> is null, return null. If
     * <code>base</code> is an instance of <code>ELFlash</code> and property is the literal string "keep", set a ThreadLocal
     * property that will be inspected by the flash on the next link in the resolution chain and return the
     * <code>ELFlash</code> instance. If <code>base</code> is an instance of <code>ELFlash</code> and <code>property</code>
     * is the literal string "now", return the result of calling <code>getRequestMap( )</code> on the
     * <code>ExternalContext</code> for the <code>FacesContext</code> for this request. Call
     * <code>setPropertyResolved(true)</code> on the <code>ELContext</code> where appropriate.
     * </p>
     *
     * @throws PropertyNotFoundException if <code>property</code> is <code>null</code>.
     */

    @Override
    public Object getValue(ELContext elContext, Object base, Object property) {
        if (null == property) {
            // take no action.
            return null;
        }

        Object result = null;

        if (null == base) {
            return null;
        }
        // If the base argument is the flash itself...
        else if (base instanceof Flash) {
            FacesContext facesContext = (FacesContext) elContext.getContext(FacesContext.class);
            ExternalContext extCtx = facesContext.getExternalContext();

            // and the property argument is "keep"...
            switch (property.toString()) {
            // Otherwise, if base is the flash, and property is "now"...
            case FLASH_KEEP_VARIABLE_NAME:
                elContext.setPropertyResolved(true);
                // then this is a request to promote the value
                // "property", which is assumed to have been previously
                // stored in request scope via the "flash.now"
                // expression, to flash scope.
                result = base;
                // Set a flag so the flash itself can look in the request
                // and promote the value to the next request
                FlashFactory ff = (FlashFactory) FactoryFinder.getFactory(FactoryFinder.FLASH_FACTORY);
                ff.getFlash(true);
                ELFlash.setKeepFlag(facesContext);
                break;
            case FLASH_NOW_VARIABLE_NAME:
                // PENDING(edburns): use FacesContext.getAttributes() instead of
                // request scope.
                Map<String, Object> requestMap = extCtx.getRequestMap();
                requestMap.put(ELFlash.FLASH_NOW_REQUEST_KEY, property);
                elContext.setPropertyResolved(true);
                result = requestMap;
                break;
            default:
                result = null;
                break;
            }
        }

        return result;
    }

    /**
     * <p>
     * Return the valid <code>Class</code> for a future set operation, which will always be <code>null</code> because sets
     * happen via the <code>MapELResolver</code> operating on the {@link ELFlash} instance as a <code>Map</code>.
     * </p>
     *
     * @throws PropertyNotFoundException if property is <code>null</code>.
     */

    @Override
    public Class<?> getType(ELContext elContext, Object base, Object property) {

        if (null != base) {
            return null;
        }
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (property.toString().equals(FLASH_VARIABLE_NAME)) {
            elContext.setPropertyResolved(true);
        }

        return null;
    }

    /**
     * <p>
     * This method will throw <code>PropertyNotWritableException</code> if called with a <code>null</code> <code>base</code>
     * and a <code>property</code> value equal to the literal string "flash". This is because set operations normally go
     * through the <code>MapELResolver</code> via the <code>ELFlash</code> <code>Map</code>.
     * </p>
     *
     * <p>
     * In other words, do not call this method directly to set a value into the flash! The only way to access the flash is
     * via the EL API.
     * </p>
     *
     * @throws PropertyNotFoundException if <code>base</code> is <code>null</code> and <code>property</code> is
     * <code>null</code>.
     * @throws PropertyNotWritableException if <code>base</code> is <code>null</code> and <code>property</code> is the
     * literal string "flash".
     */
    @Override
    public void setValue(ELContext elContext, Object base, Object property, Object value) {
        if (base != null) {
            return;
        }

        if (property == null) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }

        if (property.toString().equals(FLASH_VARIABLE_NAME)) {
            elContext.setPropertyResolved(true);
            throw new PropertyNotWritableException(property.toString());
        }
    }

    /**
     * <p>
     * Returns <code>true</code> because write operations take place via the <code>MapELResolver</code> on the actual
     * {@link ELFlash} instance.
     * </p>
     *
     * @throws PropertyNotFoundException if <code>base</code> is <code>null</code> and <code>property</code> is
     * <code>null</code>.
     */
    @Override
    public boolean isReadOnly(ELContext elContext, Object base, Object property) {
        if (base != null) {
            return false;
        }
        if (property == null) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }

        if (property.toString().equals(FLASH_VARIABLE_NAME)) {
            elContext.setPropertyResolved(true);
            return true;
        }

        return false;
    }

    /**
     * <p>
     * If <code>base</code> is non-<code>null</code> and is the literal string "flash", return <code>Object.class</code>.
     * </p>
     */
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        Class<?> result = null;
        if (null != base) {
            if (FLASH_VARIABLE_NAME.equals(base.toString())) {
                result = Object.class;
            }
        }

        return result;
    }

}
