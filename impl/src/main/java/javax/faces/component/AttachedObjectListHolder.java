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

package javax.faces.component;

import static javax.faces.component.UIComponentBase.saveAttachedState;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;

/**
 * <p>
 * Utility class to enable partial state saving of Lists of attached objects such as
 * <code>FacesListener</code>s or <code>Validator</code>s.
 * </p>
 */
@SuppressWarnings({ "unchecked" })
class AttachedObjectListHolder<T> implements PartialStateHolder {

    private boolean initialState;
    private List<T> attachedObjects = new ArrayList<>(2);

    // ------------------------------------- Methods from PartialStateHolder

    @Override
    public void markInitialState() {

        if (!attachedObjects.isEmpty()) {
            for (T t : attachedObjects) {
                if (t instanceof PartialStateHolder) {
                    ((PartialStateHolder) t).markInitialState();
                }
            }
        }
        initialState = true;

    }

    @Override
    public boolean initialStateMarked() {
        return initialState;
    }

    @Override
    public void clearInitialState() {

        if (!attachedObjects.isEmpty()) {
            for (T t : attachedObjects) {
                if (t instanceof PartialStateHolder) {
                    ((PartialStateHolder) t).clearInitialState();
                }
            }
        }
        
        initialState = false;
    }

    
    // -------------------------------------------- Methods from StateHolder

    @Override
    public Object saveState(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        
        if (attachedObjects == null) {
            return null;
        }
        
        if (initialState) {
            Object[] attachedObjects = new Object[this.attachedObjects.size()];
            boolean stateWritten = false;
            for (int i = 0, len = attachedObjects.length; i < len; i++) {
                T attachedObject = this.attachedObjects.get(i);
                if (attachedObject instanceof StateHolder) {
                    StateHolder stateHolder = (StateHolder) attachedObject;
                    if (!stateHolder.isTransient()) {
                        attachedObjects[i] = stateHolder.saveState(context);
                    }
                    if (attachedObjects[i] != null) {
                        stateWritten = true;
                    }
                }
            }
            
            return stateWritten ? attachedObjects : null;
        }

        Object[] attachedObjects = new Object[this.attachedObjects.size()];
        for (int i = 0, len = attachedObjects.length; i < len; i++) {
            attachedObjects[i] = saveAttachedState(context, this.attachedObjects.get(i));
        }
        
        return attachedObjects;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {

        if (context == null) {
            throw new NullPointerException();
        }
        
        if (state == null) {
            return;
        }

        Object[] attachedObjects = (Object[]) state;
        if (attachedObjects.length > 0 && attachedObjects[0] instanceof StateHolderSaver) {
            // overwrite the existing attachedObjects with those included
            // in the full state.
            if (this.attachedObjects != null) {
                this.attachedObjects.clear();
            } else {
                this.attachedObjects = new ArrayList<>(2);
            }
            
            for (int i = 0, len = attachedObjects.length; i < len; i++) {
                T restored = (T) ((StateHolderSaver) attachedObjects[i]).restore(context);
                if (restored != null) {
                    add(restored);
                }
            }
        } else if (this.attachedObjects != null && this.attachedObjects.size() == attachedObjects.length) {
            // Assume 1:1 relation between existing attachedObjects and state
            for (int i = 0, len = attachedObjects.length; i < len; i++) {
                T attachedObject = this.attachedObjects.get(i);
                if (attachedObject instanceof StateHolder) {
                    ((StateHolder) attachedObject).restoreState(context, attachedObjects[i]);
                }
            }
        }

    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
        // no-op
    }
    

    // ------------------------------------------------------ Public Methods

    void add(T attachedObject) {
        clearInitialState();
        attachedObjects.add(attachedObject);
    }

    void remove(T attachedObject) {
        clearInitialState();
        attachedObjects.remove(attachedObject);
    }

    T[] asArray(Class<T> type) {
        return new ArrayList<>(attachedObjects).toArray((T[]) Array.newInstance(type, attachedObjects.size()));
    }

    Iterator<T> iterator() {
        return attachedObjects.iterator();
    }

}
