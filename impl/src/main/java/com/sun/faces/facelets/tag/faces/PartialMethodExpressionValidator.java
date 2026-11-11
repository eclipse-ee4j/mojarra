/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.faces.facelets.tag.faces;

import jakarta.el.MethodExpression;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.MethodExpressionValidator;

/**
 * The {@link MethodExpressionValidator} of a <code>validator</code> attribute, which the build of the view creates again
 * on every request. Once its initial state is marked, it saves no state, so the view state holds no copy of the method
 * expression. After {@link #clearInitialState()}, it saves its full state.
 */
public final class PartialMethodExpressionValidator extends MethodExpressionValidator implements PartialStateHolder {

    private boolean initialState;

    /**
     * Needed to restore the full state.
     */
    public PartialMethodExpressionValidator() {
        super();
    }

    /**
     * @param methodExpression the method expression of the validator.
     */
    public PartialMethodExpressionValidator(MethodExpression methodExpression) {
        super(methodExpression);
    }

    @Override
    public void markInitialState() {
        initialState = true;
    }

    @Override
    public boolean initialStateMarked() {
        return initialState;
    }

    @Override
    public void clearInitialState() {
        initialState = false;
    }

    @Override
    public Object saveState(FacesContext context) {
        return initialState ? null : super.saveState(context);
    }

}
