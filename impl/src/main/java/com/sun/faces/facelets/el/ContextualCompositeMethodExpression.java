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

package com.sun.faces.facelets.el;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.component.CompositeComponentStackManager;
import com.sun.faces.util.FacesLogger;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;
import jakarta.el.MethodNotFoundException;
import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.Location;

/**
 * <p>
 * This specialized <code>MethodExpression</code> enables the evaluation of composite component expressions. Instances
 * of this expression will be created when
 * {@link com.sun.faces.facelets.tag.TagAttributeImpl#getValueExpression(jakarta.faces.view.facelets.FaceletContext, Class)}
 * is invoked and the expression represents a composite component expression (i.e. #{cc.[properties]}).
 * </p>
 *
 * <p>
 * It's important to note that these <code>MethodExpression</code>s are context sensitive in that they leverage the
 * location in which they were referenced in order to push the proper composite component to the evaluation context
 * prior to evaluating the expression itself.
 * </p>
 *
 * Here's an example:
 *
 * <pre>
 * Using Page test.xhtml
 * ---------------------------------
 *    &lt;ez:comp1 do="#{bean.action}" /&gt;
 *
 *
 * comp1.xhtml
 * ---------------------------------
 * &lt;composite:interface&gt;
 *    &lt;composite:attribute name="do" method-signature="String f()" required="true" /&gt;
 * &lt;/composite:interface&gt;
 * &lt;composite:implementation&gt;
 *    &lt;ez:nesting&gt;
 *       &lt;h:commandButton value="Click Me!" action="#{cc.attrs.do} /&gt;
 *    &lt;/ez:nesting&gt;
 * &lt;/composite:implementation&gt;
 *
 * nesting.xhtml
 * ---------------------------------
 * &lt;composite:interface /&gt;
 * &lt;composite:implementation&gt;
 *    &lt;composite:insertChildren&gt;
 * &lt;/composite:implementation&gt;
 * </pre>
 *
 * When <code>commandButton</code> is clicked, the <code>ContextualCompositeMethodExpression</code> first is looked up
 * by {@link com.sun.faces.facelets.tag.TagAttributeImpl.AttributeLookupMethodExpression} which results an instance of
 * <code>ContextualCompositeMethodExpression</code>. When this
 * <code>ContextualCompositeMethodExpression</code> is invoked, the {@link jakarta.faces.view.Location}
 * object necessary to find the proper composite component will be derived from
 * source <code>ValueExpression</code> provided at construction time. 
 * 
 * Using the derived
 * {@link jakarta.faces.view.Location}, we can find the composite component that matches 'owns' the template in which
 * the expression was defined in by comparing the path of the Location with the name and library of the
 * {@link jakarta.faces.application.Resource} instance associated with each composite component. If a matching composite
 * component is found, it will be made available to the EL by calling
 * {@link CompositeComponentStackManager#push(jakarta.faces.component.UIComponent)}.
 */
public class ContextualCompositeMethodExpression extends MethodExpression {

    private static final long serialVersionUID = -6281398928485392830L;

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.FACELETS_EL.getLogger();

    private final MethodExpression delegate;
    private final ValueExpression source;
    private final Location location;
    private String ccClientId;

    // -------------------------------------------------------- Constructors

    public ContextualCompositeMethodExpression(ValueExpression source, MethodExpression delegate) {
        this.delegate = delegate;
        this.source = source;
        location = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        UIComponent cc = UIComponent.getCurrentCompositeComponent(ctx);
        cc.subscribeToEvent(PostAddToViewEvent.class, new SetClientIdListener(this));
    }

    public ContextualCompositeMethodExpression(Location location, MethodExpression delegate) {
        this.delegate = delegate;
        this.location = location;
        source = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        UIComponent cc = UIComponent.getCurrentCompositeComponent(ctx);
        cc.subscribeToEvent(PostAddToViewEvent.class, new SetClientIdListener(this));
    }

    // ------------------------------------------- Methods from MethodExpression

    @Override
    public MethodInfo getMethodInfo(ELContext elContext) {
        return delegate.getMethodInfo(elContext);
    }

    @Override
    public Object invoke(ELContext elContext, Object[] objects) {
        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        try {
            boolean pushed = pushCompositeComponent(ctx);
            try {
                return delegate.invoke(elContext, objects);
            } finally {
                if (pushed) {
                    popCompositeComponent(ctx);
                }
            }
        } catch (ELException ele) {
            /*
             * If we got a validator exception it is actually correct to immediately bubble it up.
             */
            if (ele.getCause() != null && ele.getCause() instanceof ValidatorException) {
                throw (ValidatorException) ele.getCause();
            }

            if (source != null && ele instanceof MethodNotFoundException) {
                // special handling when an ELException handling. This is necessary
                // when there are multiple levels of composite component nesting.
                // When this happens, we need to evaluate the source expression
                // to find and invoke the MethodExpression at the next highest
                // nesting level. Is there a cleaner way to detect this case?
                try {
                    Object fallback = source.getValue(elContext);
                    if (fallback != null && fallback instanceof MethodExpression) {
                        return ((MethodExpression) fallback).invoke(elContext, objects);

                    }

                } catch (ELException ex) {

                    /*
                     * If we got a validator exception it is actually correct to immediately bubble it up.
                     */
                    if (ex.getCause() != null && ex.getCause() instanceof ValidatorException) {
                        throw (ValidatorException) ex.getCause();
                    }

                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, ele.toString());
                        LOGGER.log(Level.WARNING, "faces.facelets.el.method.expression.invoke.error: {0} {1}",
                                new Object[] { ex.toString(), source.getExpressionString() });
                    }

                    if (!(ex instanceof MethodNotFoundException)) {
                        throw ex;
                    }
                }
            }
            throw ele;
        }

    }

    // ------------------------------------------------- Methods from Expression

    @Override
    public String getExpressionString() {
        return delegate.getExpressionString();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean isLiteralText() {
        return delegate.isLiteralText();
    }

    // ----------------------------------------------------- Private Methods

    private boolean pushCompositeComponent(FacesContext ctx) {

        CompositeComponentStackManager manager = CompositeComponentStackManager.getManager(ctx);
        UIComponent foundCc = null;

        if (location != null) {
            foundCc = manager.findCompositeComponentUsingLocation(ctx, location);
        } else {
            // We need to obtain the Location of the source expression in order
            // to find the composite component that needs to be available within
            // the evaluation stack.
            if (source instanceof TagValueExpression) {
                ValueExpression orig = ((TagValueExpression) source).getWrapped();
                if (orig instanceof ContextualCompositeValueExpression) {
                    foundCc = manager.findCompositeComponentUsingLocation(ctx, ((ContextualCompositeValueExpression) orig).getLocation());
                }
            }
        }
        if (null == foundCc) {
            foundCc = ctx.getViewRoot().findComponent(ccClientId);
        }

        return manager.push(foundCc);
    }

    private void popCompositeComponent(FacesContext ctx) {

        CompositeComponentStackManager manager = CompositeComponentStackManager.getManager(ctx);
        manager.pop();

    }

    private class SetClientIdListener implements ComponentSystemEventListener {

        private ContextualCompositeMethodExpression ccME;

        public SetClientIdListener(ContextualCompositeMethodExpression ccME) {
            this.ccME = ccME;
        }

        @Override
        public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
            ccME.ccClientId = event.getComponent().getClientId();
            event.getComponent().unsubscribeFromEvent(PostAddToViewEvent.class, this);
        }
    }

}
