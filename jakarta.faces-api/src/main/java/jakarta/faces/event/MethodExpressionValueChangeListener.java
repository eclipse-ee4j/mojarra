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

package jakarta.faces.event;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <strong><span class="changed_modified_2_0 changed_modified_2_0_rev_a
 * changed_modified_2_2">MethodExpressionValueChangeListener</span></strong> is a {@link ValueChangeListener} that wraps
 * a {@link MethodExpression}. When it receives a {@link ValueChangeEvent}, it executes a method on an object identified
 * by the {@link MethodExpression}.
 * </p>
 */

public class MethodExpressionValueChangeListener implements ValueChangeListener, StateHolder {

    // ------------------------------------------------------ Instance Variables

    private MethodExpression methodExpressionOneArg = null;
    private MethodExpression methodExpressionZeroArg = null;
    private boolean isTransient;
    private final static Class<?>[] VALUECHANGE_LISTENER_ZEROARG_SIG = new Class<?>[] {};

    public MethodExpressionValueChangeListener() {
    }

    /**
     * <p>
     * <span class="changed_modified_2_0">Construct</span> a {@link ValueChangeListener} that contains a
     * {@link MethodExpression}.<span class="changed_added_2_0">To accommodate method expression targets that take no
     * arguments instead of taking a {@link ValueChangeEvent} argument</span>, the implementation of this class must take
     * the argument <code>methodExpressionOneArg</code>, extract its expression string, and create another
     * <code>MethodExpression</code> whose expected param types match those of a zero argument method. The usage
     * requirements for both of these <code>MethodExpression</code> instances are described in {@link #processValueChange}.
     * </p>
     *
     * @param methodExpressionOneArg a <code>MethodExpression</code> that points to a method that returns <code>void</code>
     * and takes a single argument of type {@link ValueChangeEvent}.
     */
    public MethodExpressionValueChangeListener(MethodExpression methodExpressionOneArg) {

        super();
        this.methodExpressionOneArg = methodExpressionOneArg;
        FacesContext context = FacesContext.getCurrentInstance();
        ELContext elContext = context.getELContext();
        methodExpressionZeroArg = context.getApplication().getExpressionFactory().createMethodExpression(elContext,
                methodExpressionOneArg.getExpressionString(), Void.class, VALUECHANGE_LISTENER_ZEROARG_SIG);
    }

    /**
     * <p>
     * Construct a {@link ValueChangeListener} that contains a {@link MethodExpression}.
     * </p>
     *
     * @param methodExpressionOneArg a method expression that takes one argument
     * @param methodExpressionZeroArg a method expression that takes zero arguments
     */
    public MethodExpressionValueChangeListener(MethodExpression methodExpressionOneArg, MethodExpression methodExpressionZeroArg) {

        super();
        this.methodExpressionOneArg = methodExpressionOneArg;
        this.methodExpressionZeroArg = methodExpressionZeroArg;

    }

    // ------------------------------------------------------- Event Method

    /**
     * <p>
     * <span class="changed_modified_2_0 changed_modified_2_2">Call</span> through to the {@link MethodExpression} passed in
     * our constructor. <span class="changed_added_2_0">First, try to invoke the <code>MethodExpression</code> passed to the
     * constructor of this instance, passing the argument {@link ValueChangeEvent} as the argument. If a
     * {@link MethodNotFoundException} is thrown, call to the zero argument <code>MethodExpression</code> derived from the
     * <code>MethodExpression</code> passed to the constructor of this instance. <span class="changed_deleted_2_2">If that
     * fails for any reason, throw an {@link AbortProcessingException}, including the cause of the failure.</span></span>
     * </p>
     *
     * @throws NullPointerException if the argument valueChangeEvent is null.
     * @throws AbortProcessingException {@inheritDoc}
     */
    @Override
    public void processValueChange(ValueChangeEvent valueChangeEvent) throws AbortProcessingException {

        if (valueChangeEvent == null) {
            throw new NullPointerException();
        }
        FacesContext context = FacesContext.getCurrentInstance();
        ELContext elContext = context.getELContext();
        // PENDING: The corresponding code in MethodExpressionActionListener
        // has an elaborate message capture, logging, and rethrowing block.
        // Why not here?
        try {
            methodExpressionOneArg.invoke(elContext, new Object[] { valueChangeEvent });
        } catch (MethodNotFoundException mnf) {
            if (null != methodExpressionZeroArg) {

                // try to invoke a no-arg version
                methodExpressionZeroArg.invoke(elContext, new Object[] {});
            }
        }
    }

    // ------------------------------------------------ Methods from StateHolder

    /**
     * <p class="changed_modified_2_0">
     * Both {@link MethodExpression} instances described in the constructor must be saved.
     * </p>
     */
    @Override
    public Object saveState(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        return new Object[] { methodExpressionOneArg, methodExpressionZeroArg };

    }

    /**
     * <p class="changed_modified_2_0">
     * Both {@link MethodExpression} instances described in the constructor must be restored.
     * </p>
     */
    @Override
    public void restoreState(FacesContext context, Object state) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (state == null) {
            return;
        }
        methodExpressionOneArg = (MethodExpression) ((Object[]) state)[0];
        methodExpressionZeroArg = (MethodExpression) ((Object[]) state)[1];

    }

    @Override
    public boolean isTransient() {

        return isTransient;

    }

    @Override
    public void setTransient(boolean newTransientValue) {

        isTransient = newTransientValue;

    }
}
