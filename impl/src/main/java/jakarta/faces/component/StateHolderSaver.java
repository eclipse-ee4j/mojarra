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

package jakarta.faces.component;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import jakarta.faces.context.FacesContext;

/**
 * <p>
 * Helper class for saving and restoring attached objects.
 * </p>
 */
class StateHolderSaver implements Serializable {

    private static final long serialVersionUID = 6470180891722042701L;

    private String className = null;
    private Serializable savedState = null;

    public static final String DYNAMIC_COMPONENT = "com.sun.faces.DynamicComponent";

    private enum StateHolderTupleIndices {
        StateHolderSaverInstance, ComponentAddedDynamically, LastMember
    }

    public boolean componentAddedDynamically() {
        boolean result = false;

        // if the Object to save implemented Serializable but not
        // StateHolder
        if (null == className && null != savedState) {
            return result;
        }

        // if the Object to save did not implement Serializable or
        // StateHolder
        if (className == null) {
            return result;
        }

        // else the object to save did implement StateHolder

        if (null != savedState) {
            // don't need to check transient, since that was done on
            // the saving side.
            Serializable[] tuple = (Serializable[]) savedState;
            result = (Boolean) tuple[StateHolderTupleIndices.ComponentAddedDynamically.ordinal()];
        }

        return result;
    }

    public StateHolderSaver(FacesContext context, Object toSave) {
        if (toSave == null) {
            className = null;
        } else {
            className = toSave.getClass().getName();
        }

        if (toSave instanceof StateHolder) {
            // do not save an attached object that is marked transient.
            if (!((StateHolder) toSave).isTransient()) {
                Serializable[] tuple = new Serializable[StateHolderTupleIndices.LastMember.ordinal()];

                tuple[StateHolderTupleIndices.StateHolderSaverInstance.ordinal()] = (Serializable) ((StateHolder) toSave).saveState(context);
                if (toSave instanceof UIComponent) {
                    tuple[StateHolderTupleIndices.ComponentAddedDynamically.ordinal()] = ((UIComponent) toSave).getAttributes().containsKey(DYNAMIC_COMPONENT)
                            ? Boolean.TRUE
                            : Boolean.FALSE;
                }
                savedState = tuple;
            } else {
                className = null;
            }
        } else if (toSave instanceof Serializable) {
            savedState = (Serializable) toSave;
            className = null;
        }
    }

    /**
     *
     * @return the restored {@link StateHolder} instance.
     */

    public Object restore(FacesContext context) throws IllegalStateException {
        // if the Object to save implemented Serializable but not
        // StateHolder
        if (null == className && null != savedState) {
            return savedState;
        }

        // if the Object to save did not implement Serializable or
        // StateHolder
        if (className == null) {
            return null;
        }

        // else the object to save did implement StateHolder
        final Object result;
        final Class<?> toRestoreClass;

        try {
            toRestoreClass = loadClass(className, this);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        try {
            result = toRestoreClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }

        if (null != savedState && result instanceof StateHolder) {
            // don't need to check transient, since that was done on
            // the saving side.
            Serializable[] tuple = (Serializable[]) savedState;
            ((StateHolder) result).restoreState(context, tuple[StateHolderTupleIndices.StateHolderSaverInstance.ordinal()]);
        }
        return result;
    }

    private static Class<?> loadClass(String name, Object fallbackClass) throws ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return Class.forName(name, false, loader);
    }
}
