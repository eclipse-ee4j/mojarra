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
import jakarta.faces.convert.Converter;

/**
 * A delegate to the CDI managed converter.
 */
public class CdiConverter implements Converter, StateHolder {

    /**
     * Stores the converter-id (if any).
     */
    private String converterId;

    /**
     * Stores a transient reference to the CDI managed converter.
     */
    private transient Converter delegate;

    /**
     * Stores the for-class (if any).
     */
    private Class<?> forClass;

    /**
     * Constructor.
     */
    public CdiConverter() {
    }

    /**
     * Constructor.
     *
     * @param converterId the converter id.
     * @param forClass the for class.
     * @param delegate the delegate.
     */
    public CdiConverter(String converterId, Class forClass, Converter delegate) {
        this.converterId = converterId;
        this.forClass = forClass;
        this.delegate = delegate;
    }

    /**
     * Get the object.
     *
     * @param facesContext the Faces context.
     * @param component the UI component.
     * @param value the value.
     * @return the object.
     */
    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
        return getDelegate(facesContext).getAsObject(facesContext, component, value);
    }

    /**
     * Get the string representation.
     *
     * @param facesContext the Faces context.
     * @param component the UI component.
     * @param value the value.
     * @return the string.
     */
    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
        return getDelegate(facesContext).getAsString(facesContext, component, value);
    }

    /**
     * Save the state.
     *
     * @param facesContext the Faces context.
     * @return the saved object.
     */
    @Override
    public Object saveState(FacesContext facesContext) {
        return new Object[] { converterId, forClass };
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
        converterId = (String) stateArray[0];
        forClass = (Class<?>) stateArray[1];
    }

    /**
     * Is the converter transient.
     *
     * @return false
     */
    @Override
    public boolean isTransient() {
        return false;
    }

    /**
     * Set the converter to transient.
     *
     * <p>
     * We ignore the call as our proxy is always non-transient.
     * </p>
     *
     * @param transientValue whether converter should be set to transient
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
    private Converter getDelegate(FacesContext facesContext) {
        if (delegate == null) {
            if (!converterId.equals("")) {
                delegate = facesContext.getApplication().createConverter(converterId);
            } else {
                delegate = facesContext.getApplication().createConverter(forClass);
            }
        }
        return delegate;
    }
}
