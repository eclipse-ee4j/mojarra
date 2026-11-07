/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.glassfish.mojarra.component;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;

import org.glassfish.mojarra.facelets.component.UIRepeat;

/**
 * The relation of an iterating component, such as {@link UIData} or {@link UIRepeat}, with the iterating components it is nested in and the iterating
 * components nested in its rows.
 * <p>
 * Before the owner leaves a row, {@link #park} takes each nested iterating component off its row, so that the nested component saves the state of its own row
 * while its clientId still identifies that row. When the owner enters that row again, {@link #unpark} puts each nested component back on its row, so that it
 * restores the state of that row. A nested component therefore never saves the state of one row as the state of another row.
 */
public final class NestedIterators {

    /**
     * The application scoped key the pattern for detecting an iterator-nested clientId is stored under.
     */
    private static final String CLIENT_ID_NESTED_IN_ITERATOR_PATTERN = "CLIENT_ID_NESTED_IN_ITERATOR_PATTERN";

    private final UIComponent owner;
    private Boolean nested;
    private Map<String, Map<UIComponent, Integer>> parked;

    /**
     * Creates the nested iterators of the given iterating component.
     *
     * @param owner the iterating component.
     */
    public NestedIterators(UIComponent owner) {
        this.owner = owner;
    }

    /**
     * Returns whether the given component iterates over its children once per row, so that its descendants have per-row clientIds and per-row state.
     *
     * @param component the component.
     * @return whether the given component is a {@link UIData} or a {@link UIRepeat}.
     */
    public static boolean isIterator(UIComponent component) {
        return component instanceof UIData || component instanceof UIRepeat;
    }

    private static boolean isNestedInIterator(FacesContext context, UIComponent component) {
        UIComponent parent = component.getParent();

        if (parent == null) {
            return false;
        }

        for (UIComponent p = parent; p != null; p = p.getParent()) {
            if (isIterator(p)) {
                return true;
            }
        }

        // https://github.com/eclipse-ee4j/mojarra/issues/4957
        // We should in long term probably introduce a common interface like UIIterable.
        // But this is solid for now as all known implementing components already follow this pattern.
        // We could theoretically even remove the above instanceof checks.
        // Application scoped rather than a constant: the separator character it is built around is configurable.
        Pattern clientIdNestedInIteratorPattern = (Pattern) context.getExternalContext().getApplicationMap()
            .computeIfAbsent(CLIENT_ID_NESTED_IN_ITERATOR_PATTERN, k -> {
                String separatorChar = Pattern.quote(String.valueOf(UINamingContainer.getSeparatorChar(context)));
                return Pattern.compile(".+" + separatorChar + "[0-9]+" + separatorChar + ".+");
            });

        return clientIdNestedInIteratorPattern.matcher(parent.getClientId(context)).matches();
    }

    /**
     * Returns whether any of the given nested iterating components is on a row, so that {@link #park} has something to do.
     *
     * @param iterators the outermost iterating components nested in the rows of the owner.
     * @return whether any of them is on a row.
     */
    public static boolean isAnyOnRow(List<UIComponent> iterators) {
        for (UIComponent component : iterators) {
            if (getRowIndex(component) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the owner is nested in an iterating component. The result is computed once per owner.
     *
     * @param context the faces context.
     * @return whether the owner is nested in an iterating component.
     */
    public boolean isNested(FacesContext context) {
        if (nested == null) {
            nested = isNestedInIterator(context, owner);
        }
        return nested;
    }

    /**
     * Takes each of the given nested iterating components which is on a row off that row, and remembers that row under the given row key of the owner.
     *
     * @param rowKey the key of the row of the owner which is being left.
     * @param iterators the outermost iterating components nested in the rows of the owner.
     */
    public void park(String rowKey, List<UIComponent> iterators) {
        Map<UIComponent, Integer> positions = null;
        for (UIComponent component : iterators) {
            int rowIndex = getRowIndex(component);
            if (rowIndex != -1) {
                if (positions == null) {
                    positions = new IdentityHashMap<>();
                }
                positions.put(component, rowIndex);
                setRowIndex(component, -1);
            }
        }
        if (positions != null) {
            if (parked == null) {
                parked = new HashMap<>();
            }
            parked.put(rowKey, positions);
        }
    }

    /**
     * Returns whether any nested iterating component is parked, so that {@link #unpark} has something to do.
     *
     * @return whether any nested iterating component is parked.
     */
    public boolean hasParked() {
        return parked != null;
    }

    /**
     * Puts each nested iterating component which was parked under the given row key of the owner back on its row.
     *
     * @param rowKey the key of the row of the owner which is being entered.
     */
    public void unpark(String rowKey) {
        if (parked == null) {
            return;
        }
        Map<UIComponent, Integer> positions = parked.remove(rowKey);
        if (parked.isEmpty()) {
            parked = null;
        }
        if (positions != null) {
            for (Entry<UIComponent, Integer> position : positions.entrySet()) {
                setRowIndex(position.getKey(), position.getValue());
            }
        }
    }

    private static int getRowIndex(UIComponent component) {
        return component instanceof UIData ? ((UIData) component).getRowIndex() : ((UIRepeat) component).getRowIndex();
    }

    private static void setRowIndex(UIComponent component, int rowIndex) {
        if (component instanceof UIData) {
            ((UIData) component).setRowIndex(rowIndex);
        }
        else {
            ((UIRepeat) component).setRowIndex(rowIndex);
        }
    }

}
