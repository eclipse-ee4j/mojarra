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

import com.sun.faces.component.CompositeComponentStackManager;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.Location;

/**
 * <p>
 * This specialized <code>ValueExpression</code> enables the evaluation of composite component expressions. Instances of
 * this expression will be created when
 * {@link com.sun.faces.facelets.tag.TagAttributeImpl#getValueExpression(jakarta.faces.view.facelets.FaceletContext, Class)}
 * is invoked and the expression represents a composite component expression (i.e. #{cc.[properties]}).
 * </p>
 *
 * <p>
 * It's important to note that these <code>ValueExpression</code>s are context sensitive in that they leverage the
 * location in which they were referenced in order to push the proper composite component to the evaluation context
 * prior to evaluating the expression itself.
 * </p>
 *
 * Here's an example:
 *
 * <pre>
 * Using Page test.xhtml
 * ---------------------------------
 *    &lt;ez:comp1 greeting="Hello!" /&gt;
 *
 *
 * comp1.xhtml
 * ---------------------------------
 * &lt;composite:interface&gt;
 *    &lt;composite:attribute name="greeting" type="java.lang.String" required="true" /&gt;
 * &lt;/composite:interface&gt;
 * &lt;composite:implementation&gt;
 *    &lt;ez:nesting&gt;
 *       &lt;h:outputText value="#{cc.attrs.greetings}" /&gt;
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
 * <p>
 * In the above example, there will be two composite components available to the runtime: <code>ez:comp1</code> and
 * <code>ez:nesting</code>.
 * </p>
 *
 * <p>
 * When &lt;h:outputText value="#{cc.attrs.greeting}" /&gt;, prior to attempting to evaluate the expression, the
 * {@link Location} object will be used to find the composite component that 'owns' the template in which the expression
 * was defined in by comparing the path of the Location with the name and library of the
 * {@link jakarta.faces.application.Resource} instance associated with each composite component. If a matching composite
 * component is found, it will be made available to the EL by calling
 * {@link CompositeComponentStackManager#push(jakarta.faces.component.UIComponent)}.
 * </p>
 */
public final class ContextualCompositeValueExpression extends ValueExpression {

    private static final long serialVersionUID = -2637560875633456679L;

    private ValueExpression originalVE;
    private Location location;

    // ---------------------------------------------------- Constructors

    /* For serialization purposes */
    public ContextualCompositeValueExpression() {
    }

    public ContextualCompositeValueExpression(Location location, ValueExpression originalVE) {

        this.originalVE = originalVE;
        this.location = location;

    }

    // ------------------------------------ Methods from ValueExpression

    @Override
    public Object getValue(ELContext elContext) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushCompositeComponent(ctx);
        try {
            return originalVE.getValue(elContext);
        } finally {
            if (pushed) {
                popCompositeComponent(ctx);
            }
        }

    }

    @Override
    public void setValue(ELContext elContext, Object o) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushCompositeComponent(ctx);
        try {
            originalVE.setValue(elContext, o);
        } finally {
            if (pushed) {
                popCompositeComponent(ctx);
            }
        }

    }

    @Override
    public boolean isReadOnly(ELContext elContext) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushCompositeComponent(ctx);
        try {
            return originalVE.isReadOnly(elContext);
        } finally {
            if (pushed) {
                popCompositeComponent(ctx);
            }
        }

    }

    @Override
    public Class<?> getType(ELContext elContext) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushCompositeComponent(ctx);
        try {
            return originalVE.getType(elContext);
        } finally {
            if (pushed) {
                popCompositeComponent(ctx);
            }
        }

    }

    @Override
    public Class<?> getExpectedType() {

        FacesContext ctx = FacesContext.getCurrentInstance();
        boolean pushed = pushCompositeComponent(ctx);
        try {
            return originalVE.getExpectedType();
        } finally {
            if (pushed) {
                popCompositeComponent(ctx);
            }
        }
    }

    @Override
    public ValueReference getValueReference(ELContext elContext) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushCompositeComponent(ctx);
        try {
            return originalVE.getValueReference(elContext);
        } finally {
            if (pushed) {
                popCompositeComponent(ctx);
            }
        }

    }

    // --------------------------------------------- Methods from Expression

    @Override
    public String getExpressionString() {
        return originalVE.getExpressionString();
    }

    @SuppressWarnings({ "EqualsWhichDoesntCheckParameterClass" })
    @Override
    public boolean equals(Object o) {
        return originalVE.equals(o);
    }

    @Override
    public int hashCode() {
        return originalVE.hashCode();
    }

    @Override
    public boolean isLiteralText() {
        return originalVE.isLiteralText();
    }

    @Override
    public String toString() {
        return originalVE.toString();
    }

    // ------------------------------------------------------ Public Methods

    /**
     * @return the {@link Location} of this <code>ValueExpression</code>
     */
    public Location getLocation() {
        return location;
    }

    // ----------------------------------------------------- Private Methods

    private boolean pushCompositeComponent(FacesContext ctx) {

        CompositeComponentStackManager manager = CompositeComponentStackManager.getManager(ctx);
        UIComponent cc = manager.findCompositeComponentUsingLocation(ctx, location);
        return manager.push(cc);

    }

    private void popCompositeComponent(FacesContext ctx) {

        CompositeComponentStackManager manager = CompositeComponentStackManager.getManager(ctx);
        manager.pop();

    }

} // END ContextualCompositeValueExpression
