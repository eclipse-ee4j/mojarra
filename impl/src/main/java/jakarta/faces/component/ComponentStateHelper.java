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

import static com.sun.faces.facelets.tag.faces.ComponentSupport.MARK_CREATED;
import static com.sun.faces.facelets.tag.faces.ComponentSupport.addToDescendantMarkIdCache;
import static com.sun.faces.util.Util.coalesce;
import static com.sun.faces.util.Util.isEmpty;
import static jakarta.faces.component.UIComponentBase.restoreAttachedState;
import static jakarta.faces.component.UIComponentBase.saveAttachedState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent.PropertyKeys;
import jakarta.faces.context.FacesContext;

/**
 * A base implementation for maps which implement the PartialStateHolder and TransientStateHolder interfaces.
 *
 * This can be used as a base-class for all state-holder implementations in components, converters and validators and
 * other implementations of the StateHolder interface.
 */
@SuppressWarnings({ "unchecked" })
class ComponentStateHelper implements StateHelper, TransientStateHelper {

    private final UIComponent component;
    private boolean isTransient;
    private final Map<Serializable, Object> deltaMap;
    private final Map<Serializable, Object> defaultMap;
    private Map<Object, Object> transientState;

    // ------------------------------------------------------------ Constructors

    public ComponentStateHelper(UIComponent component) {
        this.component = component;
        deltaMap = new HashMap<>();
        defaultMap = new HashMap<>();
        transientState = null;
    }

    // ------------------------------------------------ Methods from StateHelper

    /**
     * Put the object in the main-map and/or the delta-map, if necessary.
     *
     * @param key
     * @param value
     * @return the original value in the delta-map, if not present, the old value in the main map
     */
    @Override
    public Object put(Serializable key, Object value) {

        if (component.initialStateMarked() || value instanceof PartialStateHolder) {
            Object retVal = deltaMap.put(key, value);

            if (retVal == null) {
                return defaultMap.put(key, value);
            }

            defaultMap.put(key, value);
            return retVal;

        }

        return defaultMap.put(key, value);
    }

    /**
     * We need to remove from both maps, if we do remove an existing key.
     *
     * @param key
     * @return the removed object in the delta-map. if not present, the removed object from the main map
     */
    @Override
    public Object remove(Serializable key) {
        if (component.initialStateMarked()) {
            Object retVal = deltaMap.remove(key);

            if (retVal == null) {
                return defaultMap.remove(key);
            }

            defaultMap.remove(key);
            return retVal;
        }

        return defaultMap.remove(key);
    }

    /**
     * @see StateHelper#put(java.io.Serializable, String, Object)
     */
    @Override
    public Object put(Serializable key, String mapKey, Object value) {
        initMap(key);

        if (MARK_CREATED.equals(mapKey)) {
            if (PropertyKeys.attributes.equals(key)) {
                UIComponent parent = component.getParent();
                if (parent != null) {
                    // remember this component by its mark id
                    addToDescendantMarkIdCache(parent, component);
                }
            }
        }

        Object ret = null;
        if (component.initialStateMarked()) {
            Map<String, Object> dMap = (Map<String, Object>) deltaMap.get(key);
            ret = dMap.put(mapKey, value);
        }

        Map<String, Object> map = (Map<String, Object>) get(key);
        if (ret == null) {
            return map.put(mapKey, value);
        }

        map.put(mapKey, value);
        return ret;
    }

    private void initMap(Serializable key) {
        if (component.initialStateMarked()) {
            Map<String, Object> dMap = (Map<String, Object>) deltaMap.get(key);
            if (dMap == null) {
                dMap = new HashMap<>(5);
                deltaMap.put(key, dMap);
            }
        }

        Map<String, Object> map = (Map<String, Object>) get(key);
        if (map == null) {
            map = new HashMap<>(8);
            defaultMap.put(key, map);
        }
    }

    /**
     * Get the object from the main-map. As everything is written through from the delta-map to the main-map, this should be
     * enough.
     *
     * @param key
     */
    @Override
    public Object get(Serializable key) {
        return defaultMap.get(key);
    }

    /**
     * @see StateHelper#eval(java.io.Serializable)
     */
    @Override
    public Object eval(Serializable key) {
        return eval(key, null);
    }

    /**
     * @see StateHelper#eval(java.io.Serializable, Object)
     */
    @Override
    public Object eval(Serializable key, Object defaultValue) {
        Object retVal = get(key);

        if (retVal == null) {
            ValueExpression valueExpression = component.getValueExpression(key.toString());
            if (valueExpression != null) {
                retVal = valueExpression.getValue(component.getFacesContext().getELContext());
            }

        }

        return coalesce(retVal, defaultValue);
    }

    /**
     * @see StateHelper#eval(java.io.Serializable, Supplier)
     */
    @Override
    public Object eval(Serializable key, Supplier<Object> defaultValueSupplier) {
        Object retVal = get(key);

        if (retVal == null) {
            ValueExpression valueExpression = component.getValueExpression(key.toString());
            if (valueExpression != null) {
                retVal = valueExpression.getValue(component.getFacesContext().getELContext());
            }

        }

        if (retVal == null && defaultValueSupplier != null) {
            retVal = defaultValueSupplier.get();
        }

        return retVal;
    }

    /**
     * @see StateHelper#add(java.io.Serializable, Object)
     */
    @Override
    public void add(Serializable key, Object value) {

        initList(key);

        if (component.initialStateMarked()) {
            ((List<Object>) deltaMap.get(key)).add(value);
        }

        List<Object> items = (List<Object>) get(key);
        items.add(value);
    }

    private void initList(Serializable key) {
        if (get(key) == null) {
            List<Object> items = new ArrayList<>(4);
            defaultMap.put(key, items);
        }

        if (component.initialStateMarked()) {
            deltaMap.computeIfAbsent(key, e -> new ArrayList<>((List<Object>) get(key)));
        }
    }

    /**
     * @see StateHelper#remove(java.io.Serializable, Object)
     */
    @Override
    public Object remove(Serializable key, Object valueOrKey) {
        Object source = get(key);

        if (source instanceof Collection) {
            return removeFromList(key, valueOrKey);
        }

        if (source instanceof Map) {
            return removeFromMap(key, valueOrKey.toString());
        }

        return null;
    }

    // ------------------------------------------------ Methods from StateHolder

    /**
     * One and only implementation of save-state - makes all other implementations unnecessary.
     *
     * @param context
     * @return the saved state
     */
    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (component.initialStateMarked()) {
            return saveMap(context, deltaMap);
        }

        return saveMap(context, defaultMap);
    }

    /**
     * One and only implementation of restore state. Makes all other implementations unnecessary.
     *
     * @param context FacesContext
     * @param state the state to be restored.
     */
    @Override
    public void restoreState(FacesContext context, Object state) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (state == null) {
            return;
        }

        if (!component.initialStateMarked() && !defaultMap.isEmpty()) {
            defaultMap.clear();
            if (!isEmpty(deltaMap)) {
                deltaMap.clear();
            }
        }

        Object[] savedState = (Object[]) state;
        if (savedState[savedState.length - 1] != null) {
            component.initialState = (Boolean) savedState[savedState.length - 1];
        }

        int length = (savedState.length - 1) / 2;
        for (int i = 0; i < length; i++) {
            Object value = savedState[i * 2 + 1];
            Serializable serializable = (Serializable) savedState[i * 2];
            if (value != null) {
                if (value instanceof Collection) {
                    value = restoreAttachedState(context, value);
                } else if (value instanceof StateHolderSaver) {
                    value = ((StateHolderSaver) value).restore(context);
                } else {
                    value = value instanceof Serializable ? value : restoreAttachedState(context, value);
                }
            }

            if (value instanceof Map) {
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                    put(serializable, entry.getKey(), entry.getValue());
                }
            } else if (value instanceof List) {
                defaultMap.remove(serializable);
                deltaMap.remove(serializable);

                List<?> values = (List<?>) value;
                values.stream().forEach(o -> add(serializable, o));
            } else {
                put(serializable, value);
                handleAttribute(serializable.toString(), value);
            }
        }
    }

    /*
     * Because our renderers optimize we need to make sure that upon restore we mimic the handleAttribute of our standard
     * generated HTML components setter methods.
     */
    private void handleAttribute(String name, Object value) {
        List<String> setAttributes = (List<String>) component.getAttributes().get("jakarta.faces.component.UIComponentBase.attributesThatAreSet");
        if (setAttributes == null) {
            String className = getClass().getName();
            if (className.startsWith("jakarta.faces.component.")) {
                setAttributes = new ArrayList<>(6);
                component.getAttributes().put("jakarta.faces.component.UIComponentBase.attributesThatAreSet", setAttributes);
            }
        }

        if (setAttributes != null) {
            if (value == null) {
                ValueExpression valueExpression = component.getValueExpression(name);
                if (valueExpression == null) {
                    setAttributes.remove(name);
                }
            } else if (!setAttributes.contains(name)) {
                setAttributes.add(name);
            }
        }
    }

    /**
     * @see jakarta.faces.component.StateHolder#isTransient()
     */
    @Override
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * @see StateHolder#setTransient(boolean)
     */
    @Override
    public void setTransient(boolean newTransientValue) {
        isTransient = newTransientValue;
    }

    // --------------------------------------------------------- Private Methods

    private Object saveMap(FacesContext context, Map<Serializable, Object> map) {

        if (map.isEmpty()) {
            if (!component.initialStateMarked()) {
                // only need to propagate the component's delta status when
                // delta tracking has been disabled. We're assuming that
                // the VDL will reset the status when the view is reconstructed,
                // so no need to save the state if the saved state is the default.
                return new Object[] { component.initialStateMarked() };
            }

            return null;
        }

        Object[] savedState = new Object[map.size() * 2 + 1];

        int i = 0;

        for (Map.Entry<Serializable, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            savedState[i * 2] = entry.getKey();
            if (value instanceof Collection || value instanceof StateHolder || value instanceof Map || !(value instanceof Serializable)) {
                value = saveAttachedState(context, value);
            }
            savedState[i * 2 + 1] = value;
            i++;
        }

        if (!component.initialStateMarked()) {
            savedState[savedState.length - 1] = component.initialStateMarked();
        }

        return savedState;

    }

    private Object removeFromList(Serializable key, Object value) {
        Object ret = null;
        if (component.initialStateMarked() || value instanceof PartialStateHolder) {
            Collection<Object> deltaList = (Collection<Object>) deltaMap.get(key);
            if (deltaList != null) {
                ret = deltaList.remove(value);
                if (deltaList.isEmpty()) {
                    deltaMap.remove(key);
                }
            }
        }

        Collection<Object> list = (Collection<Object>) get(key);
        if (list != null) {
            if (ret == null) {
                ret = list.remove(value);
            } else {
                list.remove(value);
            }

            if (list.isEmpty()) {
                defaultMap.remove(key);
            }
        }

        return ret;
    }

    private Object removeFromMap(Serializable key, String mapKey) {
        Object ret = null;
        if (component.initialStateMarked()) {
            Map<String, Object> dMap = (Map<String, Object>) deltaMap.get(key);
            if (dMap != null) {
                ret = dMap.remove(mapKey);
                if (dMap.isEmpty()) {
                    deltaMap.remove(key);
                }
            }
        }
        Map<String, Object> map = (Map<String, Object>) get(key);
        if (map != null) {
            if (ret == null) {
                ret = map.remove(mapKey);
            } else {
                map.remove(mapKey);

            }
            if (map.isEmpty()) {
                defaultMap.remove(key);
            }
        }

        if (ret != null && !component.initialStateMarked()) {
            deltaMap.remove(key);
        }

        return ret;
    }

    @Override
    public Object getTransient(Object key) {
        return transientState == null ? null : transientState.get(key);
    }

    @Override
    public Object getTransient(Object key, Object defaultValue) {
        Object returnValue = transientState == null ? null : transientState.get(key);
        if (returnValue != null) {
            return returnValue;
        }

        return defaultValue;
    }

    @Override
    public Object putTransient(Object key, Object value) {
        if (transientState == null) {
            transientState = new HashMap<>();
        }

        return transientState.put(key, value);
    }

    @Override
    public void restoreTransientState(FacesContext context, Object state) {
        transientState = (Map<Object, Object>) state;
    }

    @Override
    public Object saveTransientState(FacesContext context) {
        return transientState;
    }
}
