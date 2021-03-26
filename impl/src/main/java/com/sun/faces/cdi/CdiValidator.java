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

package com.sun.faces.cdi;

import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * A delegate to the CDI managed validator.
 */
public class CdiValidator implements Validator, StateHolder {

    /**
     * Stores the validator-id.
     */
    private String validatorId;

    /**
     * Stores a transient reference to the CDI managed converter.
     */
    private transient Validator delegate;

    /**
     * Constructor.
     */
    public CdiValidator() {
    }

    /**
     * Constructor.
     *
     * @param validatorId the validator id.
     * @param delegate the delegate.
     */
    public CdiValidator(String validatorId, Validator delegate) {
        this.validatorId = validatorId;
        this.delegate = delegate;
    }

    /**
     * Save the state.
     *
     * @param facesContext the Faces context.
     * @return the saved object.
     */
    @Override
    public Object saveState(FacesContext facesContext) {
        return new Object[] { validatorId };
    }

    /**
     * Restore the state.
     *
     * @param facesContext the Faces context.
     * @param state the state.
     */
    @Override
    public void restoreState(FacesContext facesContext, Object state) {
        Object[] stateArray = (Object[]) state;
        validatorId = (String) stateArray[0];
    }

    /**
     * Is the validator transient.
     *
     * @return false
     */
    @Override
    public boolean isTransient() {
        return false;
    }

    /**
     * Set the validator to transient.
     *
     * <p>
     * We ignore the call as our proxy is always non-transient.
     * </p>
     *
     * @param transientValue the transient value.
     */
    @Override
    public void setTransient(boolean transientValue) {
    }

    /**
     * Get the delegate.
     *
     * @param facesContext the Faces context.
     * @return the delegate.
     */
    private Validator getDelegate(FacesContext facesContext) {
        if (delegate == null) {
            delegate = facesContext.getApplication().createValidator(validatorId);
        }
        return delegate;
    }

    /**
     * Validate.
     *
     * @param facesContext the Faces context.
     * @param component the UI component.
     * @param value the value.
     * @throws ValidatorException when a validation error occurs.
     */
    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object value) throws ValidatorException {
        getDelegate(facesContext).validate(facesContext, component, value);
    }
}
