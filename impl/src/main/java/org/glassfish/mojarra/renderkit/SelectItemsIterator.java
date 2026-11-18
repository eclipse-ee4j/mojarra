/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.renderkit;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectItemGroups;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.component.UISelectMany;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

import org.glassfish.mojarra.util.Util;

/**
 * <p>
 * Package private class for iterating over the set of {@link SelectItem}s for a parent {@link UISelectMany} or {@link UISelectOne}.
 * </p>
 *
 * // RELEASE_PENDING (rlubke,driscoll) performanc review
 */
public final class SelectItemsIterator<T extends SelectItem> implements Iterator<SelectItem> {

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Construct an iterator instance for the specified parent component.
     * </p>
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param parent The parent {@link UIComponent} whose children will be processed
     */
    public SelectItemsIterator(FacesContext ctx, UIComponent parent) {

        kids = parent.getChildren().listIterator();
        this.ctx = ctx;

    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>
     * Iterator over the SelectItem elements pointed at by a <code>UISelectItems</code> component, or <code>null</code>.
     * </p>
     */
    private ComponentAwareSelectItemIterator<SelectItem> items;

    /**
     * <p>
     * Iterator over the children of the parent component.
     * </p>
     */
    private ListIterator<UIComponent> kids;

    /**
     * Expose single SelectItems via an Iterator. This iterator will be reset/reused for each individual SelectItem instance encountered.
     */
    private SingleElementIterator singleItemIterator;

    /**
     * The {@link FacesContext} for the current request.
     */
    private FacesContext ctx;

    // -------------------------------------------------------- Iterator Methods

    /**
     * <p>
     * Return <code>true</code> if the iteration has more elements.
     * </p>
     */
    @Override
    public boolean hasNext() {

        if (items != null) {
            if (items.hasNext()) {
                return true;
            }
            else {
                items = null;
            }
        }
        Object next = findNextValidChild();
        while (next != null) {
            initializeItems(next);
            if (items != null) {
                return true;
            }
            else {
                next = findNextValidChild();
            }
        }
        return false;

    }

    /**
     * <p>
     * Return the next element in the iteration.
     * </p>
     *
     * @throws NoSuchElementException if there are no more elements
     */
    @Override
    public SelectItem next() {

        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (items != null) {
            return items.next();
        }
        return next();

    }

    public UIComponent currentSelectComponent() {
        UIComponent result = items.currentSelectComponent();

        return result;
    }

    /**
     * Returns the element which produced the item last returned by {@link #next()}: the member of the collection or array of a <code>UISelectItems</code>
     * value, the member of a <code>UISelectItemGroups</code> value which produced the group, the <code>SelectItem</code> of a <code>UISelectItem</code> or of a
     * single-valued <code>UISelectItems</code>, or the <code>Map.Entry</code> of a <code>Map</code> value. This is the value which the <code>var</code>
     * attribute of <code>UISelectItems</code> exposes.
     *
     * @return the current element.
     */
    public Object currentElement() {
        return items.currentElement();
    }

    /**
     * <p>
     * Throw UnsupportedOperationException.
     * </p>
     */
    @Override
    public void remove() {

        throw new UnsupportedOperationException();

    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Initializes the <code>items</code> instance variable with an <code>Iterator</code> appropriate to the UISelectItem(s) value.
     * </p>
     */
    private void initializeItems(Object kid) {

        if (kid instanceof UISelectItem) {
            UISelectItem ui = (UISelectItem) kid;
            SelectItem item = (SelectItem) ui.getValue();
            if (item == null) {
                item = new SelectItem(
                    ui.getItemValue(), ui.getItemLabel(), ui.getItemDescription(), ui.isItemDisabled(), ui.isItemEscaped(),
                    ui.isNoSelectionOption()
                );
            }
            updateSingeItemIterator(ui, item);
            items = singleItemIterator;
        }
        else if (kid instanceof UISelectItems) {
            UISelectItems ui = (UISelectItems) kid;
            Object value = ui.getValue();
            if (value != null) {
                if (value instanceof SelectItem) {
                    items = new ArrayIterator(ctx, ui, new Object[] { value });
                }
                else if (value.getClass().isArray()) {
                    items = new ArrayIterator(ctx, (UISelectItems) kid, value);
                }
                else if (value instanceof Iterable) {
                    items = new IterableItemIterator(ctx, (UISelectItems) kid, (Iterable<?>) value);
                }
                else if (value instanceof Map) {
                    items = new MapIterator(ctx, ui, (Map<?, ?>) value);
                }
                else {
                    throw new IllegalArgumentException();
                }
            }
            if (items != null && !items.hasNext()) {
                items = null;
            }
        }

    }

    /**
     * @return the next valid child for processing
     */
    private Object findNextValidChild() {

        if (kids.hasNext()) {
            Object next = kids.next();
            while (kids.hasNext() && !(next instanceof UISelectItem || next instanceof UISelectItems)) {
                next = kids.next();
            }
            if (next instanceof UISelectItem || next instanceof UISelectItems) {
                return next;
            }
        }
        return null;

    }

    /**
     * Update the <code>singleItemIterator</code> with the provided <code>item</code>
     *
     * @param item the {@link SelectItem} to expose as an Iterator
     */
    private void updateSingeItemIterator(UIComponent selectComponent, SelectItem item) {

        if (singleItemIterator == null) {
            singleItemIterator = new SingleElementIterator();
        }
        singleItemIterator.updateItem(selectComponent, item);

    }

    // ---------------------------------------------------------- Nested Classes

    /**
     * Exposes single {@link SelectItem} instances as an Iterator.
     */
    private static final class SingleElementIterator implements ComponentAwareSelectItemIterator<SelectItem> {

        private SelectItem item;
        private transient UIComponent selectComponent;
        private boolean nextCalled;

        // ----------------------------------------------- Methods from ComponentAwareSelectItemIterator

        @Override
        public UIComponent currentSelectComponent() {
            return selectComponent;
        }

        @Override
        public Object currentElement() {
            return item;
        }

        // ----------------------------------------------- Methods from Iterator

        @Override
        public boolean hasNext() {

            return !nextCalled;

        }

        @Override
        public SelectItem next() {

            if (nextCalled) {
                throw new NoSuchElementException();
            }
            nextCalled = true;
            return item;

        }

        @Override
        public void remove() {

            throw new UnsupportedOperationException();

        }

        // ----------------------------------------------------- Private Methods

        private void updateItem(UIComponent selectComponent, SelectItem item) {

            this.item = item;
            this.selectComponent = selectComponent;
            nextCalled = false;

        }

    } // END SingleElementIterator

    /**
     * Iterates over a <code>Map</code> of values exposing each entry as a SelectItem. Note that this will do so re-using the same SelectItem but changing the
     * value and label as appropriate.
     */
    private static final class MapIterator extends GenericObjectSelectItemIterator {

        private final SelectItem item = new SelectItem();
        private final FacesContext ctx;
        private final Iterator<? extends Map.Entry<?, ?>> iterator;

        // -------------------------------------------------------- Constructors

        private MapIterator(FacesContext ctx, UISelectItems sourceComponent, Map<?, ?> map) {

            super(sourceComponent);
            this.ctx = ctx;
            iterator = map.entrySet().iterator();
        }

        // ----------------------------------------------- Methods from Iterator

        @Override
        public boolean hasNext() {

            return iterator.hasNext();

        }

        @Override
        public SelectItem next() {

            Map.Entry<?, ?> entry = iterator.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            item.setLabel(key != null ? key.toString() : value.toString());
            item.setValue(value != null ? value : "");
            return getSelectItemFor(ctx, entry, item);

        }

        @Override
        public void remove() {

            throw new UnsupportedOperationException();

        }

    } // END MapIterator

    /**
     * <p>
     * Base class to support iterating over collections, arrays or maps that may or may not contain <code>SelectItem</code> instances.
     * </p>
     */
    private static abstract class GenericObjectSelectItemIterator implements ComponentAwareSelectItemIterator<SelectItem> {

        private static final String VAR = "var";

        /**
         * SelectItem that is updated based on the current Object being iterated over.
         */
        private GenericObjectSelectItem genericObjectSI;

        /**
         * The source <code>UISelectItems</code>.
         */
        protected transient UISelectItems sourceComponent;

        /**
         * The request-scoped variable under which the current object will be exposed.
         */
        private final String var;

        /**
         * The element which produced the item last returned by {@link #next()}, as {@link SelectItemsIterator#currentElement()} describes.
         */
        private transient Object currentElement;

        // -------------------------------------------------------- Constructors

        protected GenericObjectSelectItemIterator(UISelectItems sourceComponent) {

            this.sourceComponent = sourceComponent;
            var = (String) sourceComponent.getAttributes().get(VAR);

        }

        // -------------------------------------------------------- Constructors

        @Override
        public UIComponent currentSelectComponent() {
            return sourceComponent;
        }

        /**
         * {@link UISelectItemGroups#getValue()} keeps the member as the value of the group it produces, because a group has no value of its own.
         */
        @Override
        public Object currentElement() {
            return sourceComponent instanceof UISelectItemGroups ? ((SelectItem) currentElement).getValue() : currentElement;
        }

        // --------------------------------------------------- Protected Methods

        protected SelectItem getSelectItemFor(FacesContext ctx, Object value) {

            return getSelectItemFor(ctx, value, value instanceof SelectItem ? (SelectItem) value : null);

        }

        /**
         * Returns the select item for the given element. Without <code>var</code>, and for a <code>SelectItemGroup</code>, it is the given item of the element.
         * Otherwise it is created from the attributes of the source component, with the given item of the element supplying the value of each attribute which
         * is not set.
         */
        protected SelectItem getSelectItemFor(FacesContext ctx, Object element, SelectItem elementItem) {

            currentElement = element;

            if (elementItem != null && (var == null || elementItem instanceof SelectItemGroup)) {
                return elementItem;
            }

            if (genericObjectSI == null) {
                genericObjectSI = new GenericObjectSelectItem(sourceComponent, var);
            }

            genericObjectSI.updateItem(ctx, element, elementItem);
            return genericObjectSI;

        }

        // ------------------------------------------------------ Nested Classes

        /**
         * A <code>SelectItem</code> implementation to support generating unique <code>SelectItem</code> values based on <code>ValueExpressions</code> from the
         * owning {@link UISelectItems} instance.
         */
        private static final class GenericObjectSelectItem extends SelectItem {

            private static final long serialVersionUID = 1L;

            private static final String ITEM_VALUE = "itemValue";
            private static final String ITEM_LABEL = "itemLabel";
            private static final String ITEM_DESCRIPTION = "itemDescription";
            private static final String ITEM_ESCAPED = "itemLabelEscaped";
            private static final String ITEM_DISABLED = "itemDisabled";
            private static final String NO_SELECTION_OPTION = "noSelectionOption";
            private static final String NO_SELECTION_VALUE = "noSelectionValue";

            /**
             * The request-scoped variable under which the current object will be exposed.
             */
            private final String var;

            private UISelectItems sourceComponent;

            // -------------------------------------------------------- Constructors

            private GenericObjectSelectItem(UISelectItems sourceComponent, String var) {

                this.var = var;
                this.sourceComponent = sourceComponent;

            }

            // ----------------------------------------------------- Private Methods

            /**
             * Updates this item from the attributes of the source component, evaluated with <code>var</code> exposing the given element. The given item of the
             * element, if any, supplies the value of each attribute which is not set.
             */
            private void updateItem(FacesContext ctx, Object value, SelectItem elementItem) {

                Map<String, Object> reqMap = ctx.getExternalContext().getRequestMap();
                Object oldVarValue = null;
                if (var != null) {
                    oldVarValue = reqMap.put(var, value);
                }
                try {
                    Map<String, Object> attrs = sourceComponent.getAttributes();
                    updateItem(attrs, value, elementItem);
                    Object noSelectionValueResult = attrs.get(NO_SELECTION_VALUE);
                    Object noSelectionOptionResult = attrs.get(NO_SELECTION_OPTION);
                    if (null != noSelectionOptionResult) {
                        setNoSelectionOption(Util.toBoolean(noSelectionOptionResult, false));
                    }
                    else if (null != noSelectionValueResult) {
                        setNoSelectionOption(Objects.equals(getValue(), noSelectionValueResult));
                    }
                    else {
                        setNoSelectionOption(elementItem != null && elementItem.isNoSelectionOption());
                    }
                }
                finally {
                    if (var != null) {
                        if (oldVarValue != null) {
                            reqMap.put(var, oldVarValue);
                        }
                        else {
                            reqMap.remove(var);
                        }
                    }
                }

            }

            private void updateItem(Map<String, Object> attrs, Object value, SelectItem elementItem) {
                Object itemValueResult = attrs.get(ITEM_VALUE);
                Object itemLabelResult = attrs.get(ITEM_LABEL);
                Object itemDescriptionResult = attrs.get(ITEM_DESCRIPTION);
                boolean hasElementItem = elementItem != null;
                setValue(itemValueResult != null ? itemValueResult : hasElementItem ? elementItem.getValue() : value);
                setLabel(itemLabelResult != null ? itemLabelResult.toString() : hasElementItem ? elementItem.getLabel() : value.toString());
                setDescription(itemDescriptionResult != null ? itemDescriptionResult.toString() : hasElementItem ? elementItem.getDescription() : null);
                setEscape(Util.toBoolean(attrs.get(ITEM_ESCAPED), !hasElementItem || elementItem.isEscape()));
                setDisabled(Util.toBoolean(attrs.get(ITEM_DISABLED), hasElementItem && elementItem.isDisabled()));
            }

            // --------------------------------------- Methods from Serializable

            private void writeObject(ObjectOutputStream out) throws IOException {

                throw new NotSerializableException();

            }

            private void readObject(ObjectInputStream in) throws IOException {

                throw new NotSerializableException();

            }

        } // END GenericObjectSelectItem

    } // END GenericObjectSelectItemIterator

    private interface ComponentAwareSelectItemIterator<E extends Object> extends Iterator<E> {

        UIComponent currentSelectComponent();

        Object currentElement();

    }

    /**
     * Handles arrays of <code>SelectItem</code>s, generic Objects, or combintations of both.
     *
     * A single <code>GenericObjectSelectItem</code> will be leverage for any non-<code>SelectItem</code> objects encountered.
     */
    private static final class ArrayIterator extends GenericObjectSelectItemIterator {

        private FacesContext ctx;
        private Object array;
        private int count;
        private int index;

        // -------------------------------------------------------- Constructors

        private ArrayIterator(FacesContext ctx, UISelectItems sourceComponent, Object array) {

            super(sourceComponent);
            this.ctx = ctx;
            this.array = array;
            count = Array.getLength(array);

        }

        // ----------------------------------------------- Methods from Iterator

        @Override
        public boolean hasNext() {

            return index < count;

        }

        @Override
        public SelectItem next() {

            if (index >= count) {
                throw new NoSuchElementException();
            }

            return getSelectItemFor(ctx, Array.get(array, index++));

        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    } // END ArrayIterator

    /**
     * Handles Collections of <code>SelectItem</code>s, generic Objects, or combintations of both.
     *
     * A single <code>GenericObjectSelectItem</code> will be leverage for any non-<code>SelectItem</code> objects encountered.
     */
    private static final class IterableItemIterator extends GenericObjectSelectItemIterator {

        private FacesContext ctx;
        private Iterator<?> iterator;

        // -------------------------------------------------------- Constructors

        private IterableItemIterator(FacesContext ctx, UISelectItems sourceComponent, Iterable<?> iterable) {

            super(sourceComponent);
            this.ctx = ctx;
            iterator = iterable.iterator();

        }

        // ----------------------------------------------- Methods from Iterator

        @Override
        public boolean hasNext() {

            return iterator.hasNext();

        }

        @Override
        public SelectItem next() {

            return getSelectItemFor(ctx, iterator.next());

        }

        @Override
        public void remove() {

            throw new UnsupportedOperationException();

        }

    } // END CollectionItemIterator

}
