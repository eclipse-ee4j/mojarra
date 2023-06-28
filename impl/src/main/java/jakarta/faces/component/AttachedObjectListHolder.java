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

import static jakarta.faces.component.UIComponentBase.saveAttachedState;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;

import jakarta.faces.context.FacesContext;

/**
 * <p>
 * Utility class to enable partial state saving of Lists of attached objects such as <code>FacesListener</code>s or
 * <code>Validator</code>s.
 * </p>
 */
@SuppressWarnings({ "unchecked" })
class AttachedObjectListHolder<T> implements PartialStateHolder , Iterable<T> {

    private boolean initialStateMarked;
    private final List<T> attachedObjects = new ArrayList<>(2);

    // ------------------------------------- Methods from PartialStateHolder

    @Override
    public void markInitialState() {

        for (T object : attachedObjects) {
            if (object instanceof PartialStateHolder) {
                ((PartialStateHolder) object).markInitialState();
            }
        }

        initialStateMarked = true;
    }

    @Override
    public boolean initialStateMarked() {
        return initialStateMarked;
    }

    @Override
    public void clearInitialState() {

        for (T object : attachedObjects) {
            if (object instanceof PartialStateHolder) {
                ((PartialStateHolder) object).clearInitialState();
            }
        }

        initialStateMarked = false;
    }

    // -------------------------------------------- Methods from StateHolder

    @Override
    public Object saveState(FacesContext context) {
        Objects.requireNonNull(context);

        // array containing all the saved attachedObjects
        final Object[] stateSavedObjects = new Object[attachedObjects.size()];

        // if delta state changes are being tracked
        if (initialStateMarked) {
            boolean stateWritten = false;
            for (int i = 0; i < stateSavedObjects.length; i++) {
                T object = attachedObjects.get(i);
                if (object instanceof StateHolder) {
                    StateHolder stateHolder = (StateHolder) object;
                    // if not a transient object,
                    if ( !stateHolder.isTransient() ) {
                        // invoke the saveState method on every StateHolder
                        // and save in the stateSavedObjects array
                        stateSavedObjects[i] = stateHolder.saveState(context);
                        // if the object is not null, the state is written
                        stateWritten = stateSavedObjects[i] != null;
                    }
                }
            }

            return stateWritten ? stateSavedObjects : null;
        }

        // else save the state of every attached object and return the array
        else {
            for (int i=0; i < stateSavedObjects.length; i++) {
                stateSavedObjects[i] = saveAttachedState(context, attachedObjects.get(i));
            }
            return stateSavedObjects;
        }
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        Objects.requireNonNull(context);

        if (state == null) {
            return;
        }

        final Object[] savedObjects = (Object[]) state;

        if (savedObjects.length > 0 && savedObjects[0] instanceof StateHolderSaver) {
            // overwrite the existing attachedObjects with those included
            // in the full state.
            attachedObjects.clear();

            for (Object object : savedObjects) {
                T restored = (T) ((StateHolderSaver) object).restore(context);
                if (restored != null) {
                    add(restored);
                }
            }
        }

        else if (attachedObjects.size() == savedObjects.length) {
            // Assume 1:1 relation between existing attachedObjects and state
            for (int i = 0; i < savedObjects.length; i++) {
                T attachedObject = attachedObjects.get(i);
                if (attachedObject instanceof StateHolder) {
                    ((StateHolder) attachedObject).restoreState(context, savedObjects[i]);
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

    public void add(T attachedObject) {
        clearInitialState();
        attachedObjects.add(attachedObject);
    }

    public void remove(T attachedObject) {
        clearInitialState();
        attachedObjects.remove(attachedObject);
    }

    public T[] asArray(Class<T> type) {
        return new ArrayList<>(attachedObjects).toArray( (T[]) Array.newInstance(type, attachedObjects.size()) );
    }

    @Override
    public Iterator<T> iterator() {
        return attachedObjects.iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return attachedObjects.spliterator();
    }

}
